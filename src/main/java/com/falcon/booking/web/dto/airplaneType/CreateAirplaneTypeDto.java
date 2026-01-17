package com.falcon.booking.web.dto.airplaneType;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import jakarta.validation.constraints.*;

public record CreateAirplaneTypeDto (
        @NotBlank(message = "the value for producer can not be blank")
        @Size(max=100, message = "The length of the producer name can not exceed 100 characters")
        String producer,

        @NotBlank(message = "the value for producer can not be blank")
        @Size(max=100, message = "The length of the model name can not exceed 100 characters")
        String model,

        @Positive(message = "the value for economySeats must be a positive number")
        @NotNull(message = "the value for economySeats is mandatory")
        Integer economySeats,

        @PositiveOrZero(message = "the value for firstClassSeats must be zero or greater.")
        @NotNull(message = "the value for firstClassSeats is mandatory")
        Integer firstClassSeats){

    public CreateAirplaneTypeDto {
        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);
    }

}
