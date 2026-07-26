package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AirportRepositoryTest extends BaseRepositoryTest {

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

    @DisplayName("Should return airport when iata code exists")
    @Test
    void shouldReturnAirport_findByIataCode() {
        Optional<AirportEntity> airportFound = airportRepository.findByIataCode("BOG");

        assertThat(airportFound).isPresent();
        AirportEntity result = airportFound.get();
        assertThat(result.getIataCode()).isEqualTo("BOG");
        assertThat(result.getName()).isEqualTo("Aeropuerto Internacional El Dorado");
    }

    @DisplayName("Should return empty optional when iata code does not exist")
    @Test
    void shouldReturnEmpty_findByIataCode() {
        Optional<AirportEntity> airportFound = airportRepository.findByIataCode("AAA");

        assertThat(airportFound).isEmpty();
    }

    @DisplayName("Should return airport list by country")
    @Test
    void shouldReturnAirportList_findAllByCountry() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(()-> countryRepository.save(createCountry("CO", "Colombia")));


        Page<AirportEntity> airportsFound = airportRepository.findAllByCountry(
                country,
                PageRequest.of(0, 10, Sort.by("city").ascending())
        );

        assertThat(airportsFound.getContent()).hasSize(10);
        assertThat(airportsFound.getContent())
                .extracting(AirportEntity::getIataCode)
                .contains("BOG", "CTG");
    }

    @DisplayName("Should return empty list when country has no airports")
    @Test
    void shouldReturnEmptyList_findAllByCountry() {
        CountryEntity countryWithoutAirports = countryRepository.save(createCountry("UK", "United Kingdom"));

        Page<AirportEntity> airportsFound = airportRepository.findAllByCountry(
                countryWithoutAirports,
                PageRequest.of(0, 10, Sort.by("city").ascending())
        );

        assertThat(airportsFound.getContent()).isEmpty();
        assertThat(airportsFound.getTotalElements()).isZero();
    }
}
