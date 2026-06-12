package com.home.light_bot.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        return logResponse(response);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        String url = request.getURI().toString();
        String method = request.getMethod().name();
        String bodyString = new String(body, StandardCharsets.UTF_8);

        String maskedBody = maskSensitiveData(bodyString);

        log.info("--- [HTTP REQUEST] ---");
        log.info("Method: {}, URL: {}", method, url);
        if (!maskedBody.isEmpty()) {
            log.info("Body: {}", maskedBody);
        }
        log.info("Headers: {}", request.getHeaders().toSingleValueMap().keySet());
    }

    private ClientHttpResponse logResponse(ClientHttpResponse response) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

        log.info("--- [HTTP RESPONSE] ---");
        log.info("Status code: {}, Status text: {}", response.getStatusCode(), response.getStatusText());
        log.info("Body: {}", maskSensitiveData(responseBody));
        log.info("-----------------------");

        return response;
    }

    private String maskSensitiveData(String content) {
        if (content == null || content.isEmpty()) return content;

        return content
                .replaceAll("\"access_token\"\\s*:\\s*\"[^\"]+\"", "\"access_token\":\"***\"")
                .replaceAll("\"refresh_token\"\\s*:\\s*\"[^\"]+\"", "\"refresh_token\":\"***\"")
                .replaceAll("\"uid\"\\s*:\\s*\"[^\"]+\"", "\"uid\":\"***\"")
                .replaceAll("\"tid\"\\s*:\\s*\"[^\"]+\"", "\"tid\":\"***\"")
                .replaceAll("\"sign\"\\s*:\\s*\"[^\"]+\"", "\"sign\":\"***\"")
                .replaceAll("\"secret\"\\s*:\\s*\"[^\"]+\"", "\"secret\":\"***\"");
    }
}
