package com.home.light_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseGetCurrentVoltageDto {
    Integer currentVoltage;
}