package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import org.springframework.stereotype.Component;

@Component
public class PassengerMapper {

    public ResponsePassengerDto toResponseDto(PassengerEntity passengerEntity){
        String isoCode = passengerEntity.getCountryNationality() != null ?
                passengerEntity.getCountryNationality().getIsoCode() : null;

        return new ResponsePassengerDto(passengerEntity.getId(), passengerEntity.getFirstName(),
                passengerEntity.getLastName(), passengerEntity.getGender(), isoCode,
                passengerEntity.getDateOfBirth(), passengerEntity.getPassportNumber(), passengerEntity.getIdentificationNumber());
    }

    public  PassengerEntity toEntity(AddPassengerDto passengerDto) {
        return new PassengerEntity(passengerDto.firstName(), passengerDto.lastName(), passengerDto.gender(),
                passengerDto.dateOfBirth(), passengerDto.passportNumber(), passengerDto.identificationNumber() );
    }
}
