package com.falcon.booking.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record Error(
        @Schema(description = "Type of error occurred", example = "resource-not-found")
        String type,
        @Schema(description = "Cause of the error description", example = "Resource with id 27 does not exist")
        String message) { }
