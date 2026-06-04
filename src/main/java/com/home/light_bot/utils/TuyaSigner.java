package com.home.light_bot.utils;

import com.home.light_bot.properties.TuyaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_HTTPS_TYPE;

@Component
@RequiredArgsConstructor
public class TuyaSigner {
    private final TuyaProperties props;

    public String calculateSign(String t, String uri) {
        String signSource = props.getClientId() + t + buildMethodHashUri(uri);
        return hash(signSource);
    }

    public String calculateSign(String accessToken, String t, String uri) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or empty for resource requests");
        }
        String signSource = props.getClientId() + accessToken + t + buildMethodHashUri(uri);
        return hash(signSource);
    }

    private String buildMethodHashUri(String uri) {
        return TUYA_HTTPS_TYPE + "\n" + props.getContentHash() + "\n\n" + uri;
    }

    private String hash(String data) {
        try {
            Mac sha256HMAC = Mac.getInstance(props.getHmac());
            SecretKeySpec secretKey = new SecretKeySpec(
                    props.getClientSecret().getBytes(StandardCharsets.UTF_8),
                    props.getHmac()
            );
            sha256HMAC.init(secretKey);
            byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate Tuya signature", e);
        }
    }
}
