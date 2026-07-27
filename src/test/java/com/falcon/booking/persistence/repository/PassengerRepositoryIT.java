package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PassengerRepositoryIT extends BaseRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private CountryRepository countryRepository;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName(name);
        return country;
    }

    private PassengerEntity createPassenger(String identificationNumber, String passportNumber, CountryEntity country) {
        PassengerEntity passenger = new PassengerEntity();
        passenger.setFirstName("Juan");
        passenger.setLastName("Perez");
        passenger.setGender(PassengerGender.M);
        passenger.setDateOfBirth(LocalDate.of(1990, 1, 10));
        passenger.setIdentificationNumber(identificationNumber);
        passenger.setPassportNumber(passportNumber);
        passenger.setCountryNationality(country);
        return passenger;
    }

    @DisplayName("Should return true when passport number exists")
    @Test
    void shouldReturnTrue_existsByPassportNumber() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        PassengerEntity passenger = createPassenger("10001", "AB1001", country);
        passengerRepository.save(passenger);

        boolean result = passengerRepository.existsByPassportNumber("AB1001");

        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when passport number does not exist")
    @Test
    void shouldReturnFalse_existsByPassportNumber() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        PassengerEntity passenger = createPassenger("10002", "AB1002", country);
        passengerRepository.save(passenger);

        boolean result = passengerRepository.existsByPassportNumber("ZZ0000");

        assertThat(result).isFalse();
    }

    @DisplayName("Should return passenger when passport number exists")
    @Test
    void shouldReturnPassenger_findByPassportNumber() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        PassengerEntity passenger = createPassenger("10003", "AB1003", country);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository.findByPassportNumber("AB1003");

        assertThat(passengerFound).isPresent();
        assertThat(passengerFound.get().getPassportNumber()).isEqualTo("AB1003");
        assertThat(passengerFound.get().getIdentificationNumber()).isEqualTo("10003");
    }

    @DisplayName("Should return passenger when identification and country match")
    @Test
    void shouldReturnPassenger_findByIdentificationNumberAndCountryNationality() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        PassengerEntity passenger = createPassenger("10004", "AB1004", country);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository
                .findByIdentificationNumberAndCountryNationality("10004", country);

        assertThat(passengerFound).isPresent();
        assertThat(passengerFound.get().getIdentificationNumber()).isEqualTo("10004");
        assertThat(passengerFound.get().getCountryNationality().getIsoCode()).isEqualTo("CO");
    }

    @DisplayName("Should return empty when identification exists with different country")
    @Test
    void shouldReturnEmpty_findByIdentificationNumberAndCountryNationality() {
        CountryEntity countryCo = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        CountryEntity countryAr = countryRepository.findByIsoCode("AR")
                .orElseGet(() -> countryRepository.save(createCountry("AR", "Argentina")));
        PassengerEntity passenger = createPassenger("10005", "AB1005", countryCo);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository
                .findByIdentificationNumberAndCountryNationality("10005", countryAr);

        assertThat(passengerFound).isEmpty();
    }
}
