package com.falcon.booking.security.jwt;

import java.util.List;

public record JwtPayload(Long userId, String email, List<String> roles) {
}
