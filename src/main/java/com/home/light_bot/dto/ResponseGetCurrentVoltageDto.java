package com.home.light_bot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResponseGetCurrentVoltageDto {
    Integer currentVoltage;
}