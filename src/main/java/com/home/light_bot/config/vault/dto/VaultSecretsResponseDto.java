package com.home.light_bot.config.vault.dto;

import java.util.Map;

public record VaultSecretsResponseDto(
        DataWrapper data
) {
    public record DataWrapper(
            Map<String, Object> data
    ) {}
}
