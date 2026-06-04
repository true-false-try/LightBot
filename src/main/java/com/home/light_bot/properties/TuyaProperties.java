package com.home.light_bot.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.home.light_bot.config.vault.constant.TuyaConstant.TUYA_DEVICES_PATH;

@Data
@Component
public class TuyaProperties {
    @Value("${tuya.client.id}") private String clientId;
    @Value("${tuya.client.secret}") private String clientSecret;
    @Value("${tuya.sign.method}") private String signMethod;
    @Value("${tuya.algoritm.hmac}") private String hmac;
    @Value("${tuya.content_hash}") private String contentHash;
    @Value("${tuya.device.id}") private String deviceId;

    public String getTuyaDevicesPath(){
        return String.format(TUYA_DEVICES_PATH, deviceId);
    }
}
