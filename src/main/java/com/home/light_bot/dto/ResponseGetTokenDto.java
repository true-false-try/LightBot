package com.home.light_bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseGetTokenDto(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expire_time") long expireTime,
        @JsonProperty("refresh_token") String refreshToken,
        String uid
) {}
