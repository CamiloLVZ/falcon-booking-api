package com.falcon.booking.web.dto.auth;

public record LoginResponseDto (String tokenType, String accessToken) {
}
