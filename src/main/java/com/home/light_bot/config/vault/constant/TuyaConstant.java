package com.home.light_bot.config.vault.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TuyaConstant {
    public static final String TUYA_HTTPS_TYPE = "GET";
    public static final String TUYA_URL = "https://openapi.tuyaeu.com";
    public static final String TUYA_TOKEN_PATH = "/v1.0/token?grant_type=1";
    public static final String TUYA_DEVICES_PATH = "/v1.0/devices/%s/status";
}
