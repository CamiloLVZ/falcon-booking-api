package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class PassengerRepositoryTest {

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
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        PassengerEntity passenger = createPassenger("10001", "AB1234", country);
        passengerRepository.save(passenger);

        boolean result = passengerRepository.existsByPassportNumber("AB1234");

        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when passport number does not exist")
    @Test
    void shouldReturnFalse_existsByPassportNumber() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        PassengerEntity passenger = createPassenger("10001", "AB1234", country);
        passengerRepository.save(passenger);

        boolean result = passengerRepository.existsByPassportNumber("ZZ0000");

        assertThat(result).isFalse();
    }

    @DisplayName("Should return passenger when passport number exists")
    @Test
    void shouldReturnPassenger_findByPassportNumber() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        PassengerEntity passenger = createPassenger("10001", "AB1234", country);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository.findByPassportNumber("AB1234");

        assertThat(passengerFound).isPresent();
        assertThat(passengerFound.get().getPassportNumber()).isEqualTo("AB1234");
        assertThat(passengerFound.get().getIdentificationNumber()).isEqualTo("10001");
    }

    @DisplayName("Should return passenger when identification and country match")
    @Test
    void shouldReturnPassenger_findByIdentificationNumberAndCountryNationality() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        PassengerEntity passenger = createPassenger("10001", "AB1234", country);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository
                .findByIdentificationNumberAndCountryNationality("10001", country);

        assertThat(passengerFound).isPresent();
        assertThat(passengerFound.get().getIdentificationNumber()).isEqualTo("10001");
        assertThat(passengerFound.get().getCountryNationality().getIsoCode()).isEqualTo("CO");
    }

    @DisplayName("Should return empty when identification exists with different country")
    @Test
    void shouldReturnEmpty_findByIdentificationNumberAndCountryNationality() {
        CountryEntity countryCo = countryRepository.save(createCountry("CO", "Colombia"));
        CountryEntity countryAr = countryRepository.save(createCountry("AR", "Argentina"));
        PassengerEntity passenger = createPassenger("10001", "AB1234", countryCo);
        passengerRepository.save(passenger);

        Optional<PassengerEntity> passengerFound = passengerRepository
                .findByIdentificationNumberAndCountryNationality("10001", countryAr);

        assertThat(passengerFound).isEmpty();
    }
}
