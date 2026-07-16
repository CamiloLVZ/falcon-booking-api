package com.falcon.booking.feature.payment.dto;

import java.math.BigDecimal;

public record FlightPriceQuoteDto(
    Long flightId,
    BigDecimal priceEconomy,
    BigDecimal priceFirstClass
) {}
