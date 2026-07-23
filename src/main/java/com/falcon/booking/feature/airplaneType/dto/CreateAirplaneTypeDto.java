package com.falcon.booking.feature.airplaneType.dto;

import com.falcon.booking.common.utils.StringNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateAirplaneTypeDto(

        @Schema(description = "Airplane type producers name", example = "AIRBUS")
        @NotBlank(message = "the value for producer can not be blank")
        @Size(max = 100, message = "The length of the producer name can not exceed 100 characters")
        String producer,

        @Schema(description = "Airplane type model name", example = "320-200")
        @NotBlank(message = "the value for model can not be blank")
        @Size(max = 100, message = "The length of the model name can not exceed 100 characters")
        String model,

        @Schema(description = "Integer quantity of economy seats in the airplane type", example = "150")
        @Min(value = 1, message = "The airplane must have at least one economy seat")
        @NotNull(message = "the value for economySeats is mandatory")
        Integer economySeats,

        @Schema(description = "Integer quantity of first class seats in the airplane type", example = "20")
        @Min(value = 0, message = "the value for firstClassSeats must be zero or greater")
        @NotNull(message = "the value for firstClassSeats is mandatory")
        Integer firstClassSeats,

        @Schema(description = "Seat column letters (uppercase, no duplicates, e.g. 'ABCDEF')", example = "ABCDEF")
        @NotBlank(message = "Seat columns cannot be empty")
        @Pattern(regexp = "[A-Z]+", message = "Seat columns must contain only uppercase letters")
        @Size(max = 10, message = "Seat columns cannot exceed 10 characters")
        String seatColumns) {

    public CreateAirplaneTypeDto {
        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);
    }

}
