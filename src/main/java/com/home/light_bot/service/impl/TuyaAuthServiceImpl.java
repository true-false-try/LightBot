package com.home.light_bot.service.impl;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;
import com.home.light_bot.dto.ResponseGetTokenDto;
import com.home.light_bot.dto.ResponseTuyaContainerDto;
import com.home.light_bot.dto.TuyaDeviceStatusResponseDto;
import com.home.light_bot.properties.TuyaProperties;
import com.home.light_bot.service.TuyaApiService;
import com.home.light_bot.service.TuyaAuthService;
import com.home.light_bot.utils.TuyaHeadersBuilder;
import com.home.light_bot.utils.TuyaSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.home.light_bot.config.redis.constants.RedisConstants.GET_TOKEN;
import static com.home.light_bot.config.redis.constants.RedisConstants.LIGHT_BOT;
import static com.home.light_bot.config.redis.constants.RedisConstants.TOKEN_KEY_GENERATOR;
import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_TOKEN_PATH;
import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuyaAuthServiceImpl implements TuyaAuthService {

    private final RestTemplate restTemplate;
    private final TuyaSigner signer;
    private final TuyaHeadersBuilder headersBuilder;

    @Override
    @Cacheable(value = LIGHT_BOT, keyGenerator = TOKEN_KEY_GENERATOR)
    public String getToken() {
        log.info("Starting {}", GET_TOKEN);
        String t = String.valueOf(System.currentTimeMillis());
        String sign = signer.calculateSign(t, TUYA_TOKEN_PATH);

        HttpHeaders headers = headersBuilder.build(null, t, sign);

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
    @CacheEvict(value = LIGHT_BOT, key = "'" + GET_TOKEN + "'")
    public void evictTokenCache() {
        log.warn("Remove token from cache");
    }

}
