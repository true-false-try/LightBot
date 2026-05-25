package com.home.light_bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TuyaDeviceStatusResponseDto(
        @JsonProperty("result") List<StatusEntry> result,
        boolean success,
        long t,
        String tid
){
    public record StatusEntry(
                String code,
                Object value
    ) {}
}