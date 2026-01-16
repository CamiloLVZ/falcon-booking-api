package com.falcon.booking.web.dto.AirplaneTypeDto;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import jakarta.validation.constraints.*;

public record CorrectAirplaneTypeDto(
        @Size(min=1,max=100, message = "The length of the producer name can not exceed 100 characters or be blank")
        String producer,

        @Size(min=1, max=100, message = "The length of the model name can not exceed 100 characters or be blank")
        String model)
        {

    public CorrectAirplaneTypeDto {
        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);
    }

}
