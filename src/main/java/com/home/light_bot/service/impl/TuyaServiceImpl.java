package com.home.light_bot.service.impl;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;
import com.home.light_bot.dto.ResponseGetTokenDto;
import com.home.light_bot.dto.ResponseTuyaContainerDto;
import com.home.light_bot.dto.TuyaDeviceStatusResponseDto;
import com.home.light_bot.properties.TuyaProperties;
import com.home.light_bot.service.TuyaService;
import com.home.light_bot.utils.TuyaSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_TOKEN_PATH;
import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuyaServiceImpl implements TuyaService {

    private final RestTemplate restTemplate;
    private final TuyaSigner signer;
    private final TuyaProperties props;


    @Override
    public String getToken() {
        String t = String.valueOf(System.currentTimeMillis());
        String sign = signer.calculateSign(t, TUYA_TOKEN_PATH);

        HttpHeaders headers = buildHeaders(null, t, sign);

        var response = restTemplate.exchange(
                TUYA_URL + TUYA_TOKEN_PATH,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseTuyaContainerDto<ResponseGetTokenDto>>() {}
        ).getBody();

        if (response != null && response.success()) {
            return response.result().accessToken();
        }
        throw new RuntimeException("Tuya auth failed");
    }

    @Override
    public ResponseGetCurrentVoltageDto getCurrentVoltage() {
        String token = getToken(); // need added cache in redis
        String t = String.valueOf(System.currentTimeMillis());
        String path = props.getTuyaDevicesPath();
        String sign = signer.calculateSign(token, t, path);

        var response = restTemplate.exchange(
                TUYA_URL + path,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(token, t, sign)),
                TuyaDeviceStatusResponseDto.class
        ).getBody();

        return parseVoltage(response);
    }

    private HttpHeaders buildHeaders(String token, String t, String sign) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client_id", props.getClientId());
        headers.add("sign_method", props.getSignMethod());
        headers.add("t", t);
        headers.add("sign", sign);
        if (token != null) headers.add("access_token", token);
        return headers;
    }

    private ResponseGetCurrentVoltageDto parseVoltage(TuyaDeviceStatusResponseDto response) {
        return response.result().stream()
                .filter(f -> "cur_voltage".equals(f.code()))
                .findFirst()
                .map(f -> new ResponseGetCurrentVoltageDto((Integer) f.value() / 10))
                .orElse(new ResponseGetCurrentVoltageDto(null));
    }
}
