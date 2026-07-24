package com.falcon.booking.feature.airplaneType.dto;

import com.falcon.booking.common.enums.SeatClass;

public record SeatDefinition(
        Integer number,
        String label,
        SeatClass seatClass
) {}