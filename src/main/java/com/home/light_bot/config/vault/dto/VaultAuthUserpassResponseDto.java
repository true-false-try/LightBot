package com.home.light_bot.config.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VaultAuthUserpassResponseDto(
        Auth auth
) {
    public record Auth(
            @JsonProperty("client_token")
            String clientToken
    ) {}
}