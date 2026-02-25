package com.falcon.booking.web.dto.airplaneType;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateAirplaneTypeDto (

        @Schema(description = "Airplane type producers name", example = "AIRBUS")
        @NotBlank(message = "the value for producer can not be blank")
        @Size(max=100, message = "The length of the producer name can not exceed 100 characters")
        String producer,

        @Schema(description = "Airplane type model name", example = "320-200")
        @NotBlank(message = "the value for producer can not be blank")
        @Size(max=100, message = "The length of the model name can not exceed 100 characters")
        String model,

        @Schema(description = "Integer quantity of economy seats in the airplane type", example = "150")
        @Positive(message = "the value for economySeats must be a positive number")
        @NotNull(message = "the value for economySeats is mandatory")
        Integer economySeats,

        @Schema(description = "Integer quantity of first class seats in the airplane type", example = "20")
        @PositiveOrZero(message = "the value for firstClassSeats must be zero or greater.")
        @NotNull(message = "the value for firstClassSeats is mandatory")
        Integer firstClassSeats){

    public CreateAirplaneTypeDto {
        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);
    }

}
