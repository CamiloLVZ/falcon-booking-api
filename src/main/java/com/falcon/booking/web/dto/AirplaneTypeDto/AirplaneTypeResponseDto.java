package com.falcon.booking.web.dto.AirplaneTypeDto;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;

public record AirplaneTypeResponseDto(Long id, String producer, String model, Integer economySeats,
                                      Integer firstClassSeats, AirplaneTypeStatus status) { }
