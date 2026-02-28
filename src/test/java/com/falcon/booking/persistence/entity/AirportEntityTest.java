package com.falcon.booking.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AirportEntityTest {

    private AirportEntity createAirport(String iataCode, String name) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        return airport;
    }

    @DisplayName("Equals methods for entities with same iata code should return true")
    @Test
    void shouldReturnTrue_sameAirportInEquals() {
        AirportEntity airport1 = createAirport("BOG", "El Dorado");
        AirportEntity airport2 = createAirport("BOG", "Aeropuerto El Dorado");

        boolean result = airport1.equals(airport2);

        assertThat(result).isTrue();
    }

    @DisplayName("Equals methods for entities with different iata code should return false")
    @Test
    void shouldReturnFalse_differentAirportInEquals() {
        AirportEntity airport1 = createAirport("BOG", "El Dorado");
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba");

        boolean result = airport1.equals(airport2);

        assertThat(result).isFalse();
    }

    @DisplayName("Hash code for entities with same iata code should be equal")
    @Test
    void shouldBeEqual_sameAirportHashCode() {
        AirportEntity airport1 = createAirport("BOG", "El Dorado");
        AirportEntity airport2 = createAirport("BOG", "Aeropuerto El Dorado");

        int hashCode1 = airport1.hashCode();
        int hashCode2 = airport2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @DisplayName("Hash code for entities with different iata code should not be equal")
    @Test
    void shouldNotBeEqual_differentAirportHashCode() {
        AirportEntity airport1 = createAirport("BOG", "El Dorado");
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba");

        int hashCode1 = airport1.hashCode();
        int hashCode2 = airport2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }
}
