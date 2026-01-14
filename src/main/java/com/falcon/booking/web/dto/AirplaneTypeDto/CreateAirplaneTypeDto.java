package com.falcon.booking.web.dto.AirplaneTypeDto;

public record CreateAirplaneTypeDto (String producer, String model, Integer economySeats,
                                     Integer firstClassSeats){

    public CreateAirplaneTypeDto {
        producer = (producer != null) ? producer.trim() : null;
        model = (model != null) ? model.trim() : null;
    }

}
