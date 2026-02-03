package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;

public record ResponsePassengerReservationDto (

    ResponsePassengerDto passenger,
    Integer seatNumber
){ }
