package com.falcon.booking.feature.payment.dto;

import com.falcon.booking.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ResponsePaymentDto(
    String reservationNumber,
    BigDecimal totalAmount,
    PaymentStatus status,
    Instant processedAt
) {}
