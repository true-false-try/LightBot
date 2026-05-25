package com.home.light_bot.service.impl;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;
import com.home.light_bot.dto.ResponseGetTokenDto;
import com.home.light_bot.dto.ResponseTuyaContainerDto;
import com.home.light_bot.dto.TuyaDeviceStatusResponseDto;
import com.home.light_bot.service.TuyaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuyaServiceImpl implements TuyaService {
    @Value("${tuya.client.id}") private String clientId;
    @Value("${tuya.sign.method}") private String signMethod;
    @Value("${algoritm.hmac}") private String hmac;
    @Value("${tuya.client.secret}") private String clientSecret;
    @Value("${tuya.content_hash}") private String contentHash;
    @Value("${tuya.device.id}") private String deviceId;

    private final RestTemplate restTemplate;

    @Override
    public String getToken() throws Exception {
        String path = "/v1.0/token?grant_type=1";
        String t = String.valueOf(System.currentTimeMillis());
        String method = "GET";

        String stringToSign = method + "\n" + contentHash + "\n" + "" + "\n" + path;

        String signSource = clientId + t + stringToSign;
        String sign = calculateHMAC(signSource, clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.add("client_id", clientId);
        headers.add("sign_method", signMethod);
        headers.add("t", t);
        headers.add("sign", sign);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseTuyaContainerDto<ResponseGetTokenDto>> response = restTemplate.exchange(
                "https://openapi.tuyaeu.com" + path,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        ResponseTuyaContainerDto<ResponseGetTokenDto> responseBody = response.getBody();

        if (responseBody != null && responseBody.success() && responseBody.result() != null) {

            String token = responseBody.result().accessToken();
            log.debug("Get token: {}", token);

            return token;
        } else {
            String errorMsg = responseBody != null ? responseBody.msg() : "Empty response";
            throw new RuntimeException("Exception with Tuya API: " + errorMsg);
        }
    }

    @Override
    public ResponseGetCurrentVoltageDto getCurrentVoltage() throws Exception {
        String accessToken = getToken();

        String method = "GET";
        String path = "/v1.0/devices/" + deviceId + "/status";
        String t = String.valueOf(System.currentTimeMillis());

        String stringToSign = method + "\n" + contentHash + "\n" + "" + "\n" + path;

        String signSource = clientId + accessToken + t + stringToSign;

        String sign = calculateHMAC(signSource, clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.add("client_id", clientId);
        headers.add("sign_method", signMethod);
        headers.add("access_token", accessToken);
        headers.add("t", t);
        headers.add("sign", sign);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TuyaDeviceStatusResponseDto> responseTuya = restTemplate.exchange(
                "https://openapi.tuyaeu.com" + path,
                HttpMethod.GET,
                entity,
                TuyaDeviceStatusResponseDto.class
        );


       TuyaDeviceStatusResponseDto tuyaContainerDto = responseTuya.getBody();

       log.debug("CURL headers --> client_id: {}, access_token: {}, sign: {}, t: {}", clientId, accessToken, sign, t);

       return  tuyaContainerDto.result().stream()
                .filter(field -> "cur_voltage".equals(field.code()))
                .findFirst()
                .map(field -> ResponseGetCurrentVoltageDto.builder().currentVoltage(
                        calculateVoltage((Integer) field.value())
                ).build())
                .orElseGet(() -> ResponseGetCurrentVoltageDto.builder().currentVoltage(null).build());
    }

    private String calculateHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMAC = Mac.getInstance(hmac);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), hmac);
        sha256HMAC.init(secretKey);
        byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) { result.append(String.format("%02x", b)); }
        return result.toString().toUpperCase();
    }

    private Integer calculateVoltage(Integer tuyaVoltage) {
        return tuyaVoltage / 10;
    }
}
