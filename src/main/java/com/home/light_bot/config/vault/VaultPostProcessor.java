package com.home.light_bot.config.vault;

import com.home.light_bot.config.vault.dto.VaultAuthUserpassRequestDto;
import com.home.light_bot.config.vault.dto.VaultAuthUserpassResponseDto;
import com.home.light_bot.config.vault.dto.VaultSecretsResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.vault.VaultException;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.web.client.RestTemplate;


import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.home.light_bot.config.vault.constant.VaultConstants.HEADER_VAULT_TOKEN;
import static com.home.light_bot.config.vault.constant.VaultConstants.PROTOCOL_HTTPS;
import static com.home.light_bot.config.vault.constant.VaultConstants.URL_CONFIG_SUFFIX;
import static com.home.light_bot.config.vault.constant.VaultConstants.URL_USERPASS_SUFFIX;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_CREDENTIAL_EXCEPTION;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_HOST;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_HOST_PORT_EXCEPTION;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_JKS_EXCEPTION;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_JKS_PASSWORD;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_JKS_PATH;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_LOGIN;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_PASSWORD;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_PORT;
import static com.home.light_bot.config.vault.constant.VaultConstants.VAULT_SECRETS;


@Slf4j
@Configuration
public class VaultPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(@NotNull ConfigurableEnvironment environment, @NotNull SpringApplication application) {
        Map<String, String> vaultVariables = getVaultCredentialsAndHosAndPort();

        String host = vaultVariables.get(VAULT_HOST);
        String port = vaultVariables.get(VAULT_PORT);
        String login = vaultVariables.get(VAULT_LOGIN);
        String password = vaultVariables.get(VAULT_PASSWORD);
        String jksPath = vaultVariables.get(VAULT_JKS_PATH);
        String jksPassword = vaultVariables.get(VAULT_JKS_PASSWORD);

        try {
            VaultEndpoint vaultEndpoint = getVaultEndpoint(host, port);
            RestTemplate restTemplate = createRestTemplateTrustVaultCertificate(jksPath,jksPassword);
            ResponseEntity<VaultAuthUserpassResponseDto> responseClientToken = getClientToken(restTemplate, vaultEndpoint, login, password);
            log.debug("Login response has token");

            if (responseClientToken.getStatusCode().is2xxSuccessful()) {
                String clientToken = Objects.requireNonNull(responseClientToken.getBody()).auth().clientToken();
                Map<String, Object> secrets = getKv2SecretWithToken(restTemplate, vaultEndpoint, clientToken)
                        .data()
                        .data();

                setEnvironment(secrets, environment);
                log.info("Vault secrets successfully added to Spring Environment");
            }

        } catch (Exception ex) {
            throw new VaultException("Exception login for Vault: " + ex.getMessage(), ex);
        }
    }


    private  ResponseEntity<VaultAuthUserpassResponseDto> getClientToken(RestTemplate restTemplate, VaultEndpoint vaultEndpoint, String login, String password) {
        String loginUrl = vaultEndpoint.createUriString(String.format(URL_USERPASS_SUFFIX, login));
        VaultAuthUserpassRequestDto logInRequestDto = new VaultAuthUserpassRequestDto(password);

        return restTemplate.postForEntity(loginUrl, logInRequestDto, VaultAuthUserpassResponseDto.class);
    }

    private VaultSecretsResponseDto getKv2SecretWithToken(RestTemplate restTemplate,
                                                      VaultEndpoint vaultEndpoint,
                                                      String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_VAULT_TOKEN, token);
        String secretUrl = vaultEndpoint.createUriString(URL_CONFIG_SUFFIX);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<VaultSecretsResponseDto> resp = restTemplate.exchange(
                secretUrl,
                HttpMethod.GET,
                entity,
                VaultSecretsResponseDto.class
        );

        if (!resp.getStatusCode().is2xxSuccessful()) {
            int status = resp.getStatusCode().value();
            if (status == 404)
                throw new VaultException("Secret not found: " + URL_CONFIG_SUFFIX);
            if (status == 403)
                throw new VaultException("Forbidden: token has no access to " + URL_CONFIG_SUFFIX);

            throw new VaultException("Vault returned " + resp.getStatusCode() + " for " + URL_CONFIG_SUFFIX);
        }

        VaultSecretsResponseDto body = resp.getBody();
        if (body == null || body.data() == null) {
            throw new VaultException("Empty Vault response for " + URL_CONFIG_SUFFIX);
        }
        return body;
    }

    private RestTemplate createRestTemplateTrustVaultCertificate(String jksPath, String jksPassword) {
        try {

            FileSystemResource resource = new FileSystemResource(jksPath);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(resource.getInputStream(), jksPassword.toCharArray());

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(trustStore, null)
                    .build();

            var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build();

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

        } catch (Exception ex) {
            throw new RuntimeException("Exception when creating RestTemplate with custom certificate", ex);
        }
    }

    private VaultEndpoint getVaultEndpoint(String host, String port) {
        VaultEndpoint vaultEndpoint = new VaultEndpoint();
        vaultEndpoint.setScheme(PROTOCOL_HTTPS);
        vaultEndpoint.setHost(host);
        vaultEndpoint.setPort(Integer.parseInt(port));
        return vaultEndpoint;
    }

    private Map <String, String> getVaultCredentialsAndHosAndPort() {
        String login = System.getenv(VAULT_LOGIN);
        String password = System.getenv(VAULT_PASSWORD);
        String host = System.getenv(VAULT_HOST);
        String port = System.getenv(VAULT_PORT);
        String jksPath = System.getenv(VAULT_JKS_PATH);
        String jksPassword = System.getenv(VAULT_JKS_PASSWORD);

        if (login == null || password == null) {
            throw new IllegalStateException(String.format(VAULT_CREDENTIAL_EXCEPTION, VAULT_LOGIN, VAULT_PASSWORD));
        } else if (host == null || port == null) {
            throw new IllegalStateException(String.format(VAULT_HOST_PORT_EXCEPTION, VAULT_HOST, VAULT_PORT));
        } else if (jksPath == null || jksPassword == null)
            throw new IllegalStateException(String.format(VAULT_JKS_EXCEPTION, VAULT_JKS_PATH, VAULT_JKS_PASSWORD));

        return new HashMap<>(Map.of(
                VAULT_LOGIN, login,
                VAULT_PASSWORD, password,
                VAULT_HOST, host,
                VAULT_PORT, port,
                VAULT_JKS_PATH, jksPath,
                VAULT_JKS_PASSWORD, jksPassword
        ));
    }

    private void setEnvironment(Map<String, Object> secrets, ConfigurableEnvironment environment) {
        environment.getPropertySources().addFirst(
                new MapPropertySource(VAULT_SECRETS, secrets)
        );
    }
}
