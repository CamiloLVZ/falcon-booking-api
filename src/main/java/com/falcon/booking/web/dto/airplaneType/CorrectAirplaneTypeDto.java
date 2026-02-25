package com.falcon.booking.web.dto.airplaneType;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CorrectAirplaneTypeDto(

        @Schema(description = "New airplane type producers name", example = "BOEING")
        @Size(min=1,max=100, message = "The length of the producer name can not exceed 100 characters or be blank")
        String producer,

        @Schema(description = "New airplane type model name", example = "737-300")
        @Size(min=1, max=100, message = "The length of the model name can not exceed 100 characters or be blank")
        String model)
        {

    public CorrectAirplaneTypeDto {
        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);
    }

}
