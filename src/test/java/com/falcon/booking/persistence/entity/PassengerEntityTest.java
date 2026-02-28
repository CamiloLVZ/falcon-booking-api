package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.PassengerGender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class PassengerEntityTest {

    private CountryEntity createCountry(String isoCode) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setIsoCode(isoCode);
        countryEntity.setName("Country " + isoCode);
        return countryEntity;
    }

    private PassengerEntity createPassenger(String identificationNumber, String passportNumber, CountryEntity country) {
        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFirstName("Juan");
        passengerEntity.setLastName("Perez");
        passengerEntity.setGender(PassengerGender.M);
        passengerEntity.setDateOfBirth(LocalDate.of(1990, 1, 10));
        passengerEntity.setIdentificationNumber(identificationNumber);
        passengerEntity.setPassportNumber(passportNumber);
        passengerEntity.setCountryNationality(country);
        return passengerEntity;
    }

    @DisplayName("Should normalize fields in constructor")
    @Test
    void shouldNormalizeFields_constructor() {
        PassengerEntity passengerEntity = new PassengerEntity(
                "  juan ",
                " perez  ",
                PassengerGender.M,
                LocalDate.of(1990, 1, 10),
                " ab1234 ",
                "  10001 "
        );

        assertThat(passengerEntity.getFirstName()).isEqualTo("JUAN");
        assertThat(passengerEntity.getLastName()).isEqualTo("PEREZ");
        assertThat(passengerEntity.getPassportNumber()).isEqualTo("AB1234");
        assertThat(passengerEntity.getIdentificationNumber()).isEqualTo("10001");
    }

    @DisplayName("Should normalize firstName, lastName, passport and identification")
    @Test
    void shouldNormalizeFields_setters() {
        PassengerEntity passengerEntity = new PassengerEntity();

        passengerEntity.setFirstName("  juan ");
        passengerEntity.setLastName(" perez  ");
        passengerEntity.setPassportNumber(" ab1234 ");
        passengerEntity.setIdentificationNumber("  10001 ");

        assertThat(passengerEntity.getFirstName()).isEqualTo("JUAN");
        assertThat(passengerEntity.getLastName()).isEqualTo("PEREZ");
        assertThat(passengerEntity.getPassportNumber()).isEqualTo("AB1234");
        assertThat(passengerEntity.getIdentificationNumber()).isEqualTo("10001");
    }

    @DisplayName("Equals methods for entities with same country and identification should return true")
    @Test
    void shouldReturnTrue_samePassengerInEquals() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passenger1 = createPassenger("10001", "AB1234", country);
        PassengerEntity passenger2 = createPassenger("10001", "ZZ9999", country);

        boolean result = passenger1.equals(passenger2);

        assertThat(result).isTrue();
    }

    @DisplayName("Equals methods for entities with different identification should return false")
    @Test
    void shouldReturnFalse_differentIdentificationInEquals() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passenger1 = createPassenger("10001", "AB1234", country);
        PassengerEntity passenger2 = createPassenger("10002", "AB1234", country);

        boolean result = passenger1.equals(passenger2);

        assertThat(result).isFalse();
    }

    @DisplayName("Equals methods for entities with different country should return false")
    @Test
    void shouldReturnFalse_differentCountryInEquals() {
        PassengerEntity passenger1 = createPassenger("10001", "AB1234", createCountry("CO"));
        PassengerEntity passenger2 = createPassenger("10001", "AB1234", createCountry("AR"));

        boolean result = passenger1.equals(passenger2);

        assertThat(result).isFalse();
    }

    @DisplayName("Hash code for entities with same country and identification should be equal")
    @Test
    void shouldBeEqual_samePassengerHashCode() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passenger1 = createPassenger("10001", "AB1234", country);
        PassengerEntity passenger2 = createPassenger("10001", "ZZ9999", country);

        int hashCode1 = passenger1.hashCode();
        int hashCode2 = passenger2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @DisplayName("Hash code for entities with different country should not be equal")
    @Test
    void shouldNotBeEqual_differentPassengerHashCode() {
        PassengerEntity passenger1 = createPassenger("10001", "AB1234", createCountry("CO"));
        PassengerEntity passenger2 = createPassenger("10001", "AB1234", createCountry("AR"));

        int hashCode1 = passenger1.hashCode();
        int hashCode2 = passenger2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }
}
