package com.home.light_bot.dto;

public record ResponseTuyaContainerDto<T>(
        boolean success,
        Integer code,
        String msg,
        long t,
        String tid,
        T result
) {}
