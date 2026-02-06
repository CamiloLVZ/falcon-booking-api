package com.falcon.booking.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CountryEntityTest {

    private CountryEntity createCountry(String isoCode, String name){
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName(name);
        return country;
    }

    @DisplayName("Equals methods for entities with same isoCode should return true")
    @Test
    void shouldReturnTrue_sameCountryInEquals(){
        CountryEntity country1 = createCountry("CO", "Colombia");
        CountryEntity country2 = createCountry("CO", "Republica de Colombia");

        boolean result = country1.equals(country2);

        assertThat(result).isTrue();
    }

    @DisplayName("Equals methods for entities with different isoCode should return false")
    @Test
    void shouldReturnFalse_differentCountryInEquals(){
        CountryEntity country1 = createCountry("CO", "Colombia");
        CountryEntity country2 = createCountry("CU", "Colombia");

        boolean result = country1.equals(country2);

        assertThat(result).isFalse();
    }

    @DisplayName("Hash code for entities with same isoCode should be equal")
    @Test
    void shouldBeEqual_sameCountryHashCode(){
        CountryEntity country1 = createCountry("CO", "Colombia");
        CountryEntity country2 = createCountry("CO", "Republica de Colombia");

        int hashCode1 = country1.hashCode();
        int hashCode2 = country2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @DisplayName("Hash code for entities with different isoCode should not be equal")
    @Test
    void shouldNotBeEqual_differentCountryHashCode(){
        CountryEntity country1 = createCountry("CO", "Colombia");
        CountryEntity country2 = createCountry("CU", "Colombia");

        int hashCode1 = country1.hashCode();
        int hashCode2 = country2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }
}
