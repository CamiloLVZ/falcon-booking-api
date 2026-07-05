package com.falcon.booking.feature.auth.dto;

public record LoginResponseDto (String tokenType, String accessToken) {
}
