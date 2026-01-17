package com.falcon.booking.web.dto.airplaneType;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;

public record ResponseAirplaneTypeDto(Long id, String producer, String model, Integer economySeats,
                                      Integer firstClassSeats, AirplaneTypeStatus status) { }
