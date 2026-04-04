package com.falcon.booking.web.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @Schema(description = "User email address", example = "johndoe@example.com")
        @NotBlank(message = "Email can not be blank")
        @Size(min = 8, max = 128, message = "email must be an alphanumeric value with 8 to 128 characters")
        String email,

        @Schema(description = "User password", example = "f4lc0nb00k1ng")
        @NotBlank(message = "Password can not be blank")
        @Size(min = 8, max = 128, message = "password must be an alphanumeric value with 8 to 128 characters")
        String password){
}
