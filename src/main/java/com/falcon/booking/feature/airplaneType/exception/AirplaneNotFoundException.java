package com.falcon.booking.feature.airplaneType.exception;

public class AirplaneNotFoundException extends RuntimeException {
    public AirplaneNotFoundException(Long id) {
        super("Airplane Type with id: "+ id +" not found");
    }

    public AirplaneNotFoundException(String producer, String model) {

        super("Airplane Type " + producer + " - " + model + " not found");
    }
}

