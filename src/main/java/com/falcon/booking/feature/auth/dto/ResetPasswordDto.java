package com.falcon.booking.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @Pattern(regexp = "\\d{6}", message = "Reset code must contain exactly 6 digits")
        String code,

        @NotBlank(message = "Password can not be blank")
        @Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
        String password
) {
}
