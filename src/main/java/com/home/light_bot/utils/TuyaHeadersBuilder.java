package com.home.light_bot.utils;

import com.home.light_bot.properties.TuyaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TuyaHeadersBuilder {
    private final TuyaProperties props;

    public HttpHeaders build(String token, String t, String sign) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client_id", props.getClientId());
        headers.add("sign_method", props.getSignMethod());
        headers.add("t", t);
        headers.add("sign", sign);
        if (token != null) headers.add("access_token", token);
        return headers;
    }
}
