package com.falcon.booking.domain.mapper;

import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerMapperTest {

    private final PassengerMapper passengerMapper = new PassengerMapper();

    @DisplayName("Should map passenger entity to response dto including nationality iso code")
    @Test
    void shouldMapPassengerEntityToResponseDto() {
        CountryEntity country = new CountryEntity();
        country.setIsoCode("CO");

        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setId(7L);
        passengerEntity.setFirstName("Maria");
        passengerEntity.setLastName("Lopez");
        passengerEntity.setGender(PassengerGender.F);
        passengerEntity.setCountryNationality(country);
        passengerEntity.setDateOfBirth(LocalDate.of(1995, 6, 15));
        passengerEntity.setPassportNumber("PA123");
        passengerEntity.setIdentificationNumber("100200300");

        ResponsePassengerDto result = passengerMapper.toResponseDto(passengerEntity);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.firstName()).isEqualTo("MARIA");
        assertThat(result.lastName()).isEqualTo("LOPEZ");
        assertThat(result.nationalityIsoCode()).isEqualTo("CO");
        assertThat(result.passportNumber()).isEqualTo("PA123");
    }

    @DisplayName("Should map add passenger dto to entity normalizing text values")
    @Test
    void shouldMapAddPassengerDtoToEntity() {
        AddPassengerDto addPassengerDto = new AddPassengerDto(
                " juan ",
                " perez ",
                PassengerGender.M,
                "co",
                LocalDate.of(1990, 1, 1),
                " ab123 ",
                " 12345 "
        );

        PassengerEntity result = passengerMapper.toEntity(addPassengerDto);

        assertThat(result.getFirstName()).isEqualTo("JUAN");
        assertThat(result.getLastName()).isEqualTo("PEREZ");
        assertThat(result.getGender()).isEqualTo(PassengerGender.M);
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getPassportNumber()).isEqualTo("AB123");
        assertThat(result.getIdentificationNumber()).isEqualTo("12345");
    }
}
