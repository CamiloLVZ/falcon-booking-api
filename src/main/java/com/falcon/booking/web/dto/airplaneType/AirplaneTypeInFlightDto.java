package com.falcon.booking.web.dto.airplaneType;

public record AirplaneTypeInFlightDto(String producer, String model, Integer economySeats,
                                      Integer firstClassSeats) {
}
