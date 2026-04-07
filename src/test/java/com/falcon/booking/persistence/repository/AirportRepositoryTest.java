package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class AirportRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private CountryRepository countryRepository;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName(name);
        return country;
    }

    private AirportEntity createAirport(String iataCode, String name, String city, CountryEntity country) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    @DisplayName("Should return airport when iata code exists")
    @Test
    void shouldReturnAirport_findByIataCode() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        airportRepository.save(airport);

        Optional<AirportEntity> airportFound = airportRepository.findByIataCode("BOG");

        assertThat(airportFound).isPresent();
        AirportEntity result = airportFound.get();
        assertThat(result.getIataCode()).isEqualTo("BOG");
        assertThat(result.getName()).isEqualTo("El Dorado");
    }

    @DisplayName("Should return empty optional when iata code does not exist")
    @Test
    void shouldReturnEmpty_findByIataCode() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        airportRepository.save(airport);

        Optional<AirportEntity> airportFound = airportRepository.findByIataCode("MDE");

        assertThat(airportFound).isEmpty();
    }

    @DisplayName("Should return airport list by country")
    @Test
    void shouldReturnAirportList_findAllOrderByCityByCountryByOrderByIataCode() {
        CountryEntity country = countryRepository.save(createCountry("CO", "Colombia"));
        AirportEntity airport1 = createAirport("BOG", "El Dorado", "Bogota", country);
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba", "Medellin", country);
        airportRepository.save(airport1);
        airportRepository.save(airport2);

        List<AirportEntity> airportsFound = airportRepository.findAllByCountryOrderByCityAsc(country);

        assertThat(airportsFound).hasSize(2);
    }

    @DisplayName("Should return empty list when country has no airports")
    @Test
    void shouldReturnEmptyList_findAllOrderByCityByCountryByOrderByIataCode() {
        CountryEntity countryWithAirports = countryRepository.save(createCountry("CO", "Colombia"));
        CountryEntity countryWithoutAirports = countryRepository.save(createCountry("US", "United States"));
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", countryWithAirports);
        airportRepository.save(airport);

        List<AirportEntity> airportsFound = airportRepository.findAllByCountryOrderByCityAsc(countryWithoutAirports);

        assertThat(airportsFound).isEmpty();
    }
}
