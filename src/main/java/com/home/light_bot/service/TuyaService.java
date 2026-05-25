package com.home.light_bot.service;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;

public interface TuyaService extends TuyaAuthService {
    ResponseGetCurrentVoltageDto getCurrentVoltage() throws Exception;
}
