package com.home.light_bot.service.impl;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;
import com.home.light_bot.dto.TuyaDeviceStatusResponseDto;
import com.home.light_bot.properties.TuyaProperties;
import com.home.light_bot.service.TuyaApiService;
import com.home.light_bot.service.TuyaAuthService;
import com.home.light_bot.utils.TuyaHeadersBuilder;
import com.home.light_bot.utils.TuyaSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuyaApiServiceImpl implements TuyaApiService {

    private final RestTemplate restTemplate;
    private final TuyaSigner signer;
    private final TuyaProperties props;

    private final TuyaAuthService authService;
    private final TuyaHeadersBuilder headersBuilder;

    @Override
    public ResponseGetCurrentVoltageDto getCurrentVoltage() throws Exception {
        log.info("Starting {}", Thread.currentThread().getStackTrace()[1].getMethodName());

        String token = authService.getToken();
        try {
            return executeVoltageRequest(token);
        } catch (HttpClientErrorException.Unauthorized ex) {
            authService.evictTokenCache();
            return executeVoltageRequest(token);
        }
    }

    private ResponseGetCurrentVoltageDto executeVoltageRequest(String token) {
        String t = String.valueOf(System.currentTimeMillis());
        String path = props.getTuyaDevicesPath();
        String sign = signer.calculateSign(token, t, path);

        var response = restTemplate.exchange(
                TUYA_URL + path,
                HttpMethod.GET,
                new HttpEntity<>(headersBuilder.build(token, t, sign)),
                TuyaDeviceStatusResponseDto.class
        ).getBody();

        return parseVoltage(Objects.requireNonNull(response));
    }

    private ResponseGetCurrentVoltageDto parseVoltage(TuyaDeviceStatusResponseDto response) {
        return response.result().stream()
                .filter(f -> "cur_voltage".equals(f.code()))
                .findFirst()
                .map(f -> new ResponseGetCurrentVoltageDto((Integer) f.value() / 10))
                .orElse(new ResponseGetCurrentVoltageDto(null));
    }
}
