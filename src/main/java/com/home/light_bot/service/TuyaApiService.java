package com.home.light_bot.service;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;

public interface TuyaApiService {
    ResponseGetCurrentVoltageDto getCurrentVoltage() throws Exception;
}
