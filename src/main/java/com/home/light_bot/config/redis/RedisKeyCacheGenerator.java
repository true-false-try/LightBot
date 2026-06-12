package com.home.light_bot.config.redis;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


import static com.home.light_bot.config.redis.constants.RedisConstants.TOKEN_KEY_GENERATOR;

@Component(TOKEN_KEY_GENERATOR)
@RequiredArgsConstructor
public class RedisKeyCacheGenerator implements KeyGenerator {

    @Override
    public @NonNull Object generate(@NonNull Object target, @NonNull Method method, Object @NonNull ... params) {
        return method.getName();
    }

}
