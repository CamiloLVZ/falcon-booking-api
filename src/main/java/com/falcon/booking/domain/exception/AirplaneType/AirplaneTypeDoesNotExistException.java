package com.falcon.booking.domain.exception.AirplaneType;

public class AirplaneTypeDoesNotExistException extends RuntimeException {
    public AirplaneTypeDoesNotExistException(Long id) {

        super("The Airplane Type with id: "+ id +" does not exist");
    }

    public AirplaneTypeDoesNotExistException(String producer, String model) {

        super("The Airplane Type " + producer + " - " + model + " does not exist");
    }
}

