package com.falcon.booking.persistence.specification;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.persistence.repository.BaseRepositoryTest;
import com.falcon.booking.persistence.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirportSpecificationsIT extends BaseRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private CountryRepository countryRepository;

    private CountryEntity colombia;
    private CountryEntity usa;

    @BeforeEach
    void setUp() {
        colombia = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        usa = countryRepository.findByIsoCode("US")
                .orElseGet(() -> countryRepository.save(createCountry("US", "United States")));

        saveIfNotExists("ZZZ", "Test Airport One", colombia);
        saveIfNotExists("YYY", "Test Airport Two", colombia);
        saveIfNotExists("XXX", "Test Airport Three", usa);
    }

    private void saveIfNotExists(String iataCode, String name, CountryEntity country) {
        if (airportRepository.findByIataCode(iataCode).isEmpty()) {
            airportRepository.save(createAirport(iataCode, name, country));
        }
    }

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity c = new CountryEntity();
        c.setIsoCode(isoCode);
        c.setName(name);
        return c;
    }

    private AirportEntity createAirport(String iataCode, String name, CountryEntity country) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        airport.setCity("City " + iataCode);
        airport.setCountry(country);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    @DisplayName("Should filter airports by country ISO code")
    @Test
    void shouldFilterByCountryIsoCode() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode("CO");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        result.forEach(a -> assertThat(a.getCountry().getIsoCode()).isEqualTo("CO"));
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
        assertThat(result).anyMatch(a -> a.getIataCode().equals("YYY"));
    }

    @DisplayName("Should return all airports when ISO code is null")
    @Test
    void shouldReturnAllWhenIsoCodeIsNull() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode(null);

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
    }

    @DisplayName("Should return all airports when ISO code is blank")
    @Test
    void shouldReturnAllWhenIsoCodeIsBlank() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode("   ");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
    }

    @DisplayName("Should filter airports by name or IATA containing text")
    @Test
    void shouldFilterByNameOrIataContains() {
        Specification<AirportEntity> spec = AirportSpecifications.nameOrIataContains("Test Airport One");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIataCode()).isEqualTo("ZZZ");
    }

    @DisplayName("Should filter by IATA code when search matches iataCode")
    @Test
    void shouldFilterByIataCode() {
        Specification<AirportEntity> spec = AirportSpecifications.nameOrIataContains("YYY");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIataCode()).isEqualTo("YYY");
    }

    @DisplayName("Should return all airports when search is null")
    @Test
    void shouldReturnAllWhenSearchIsNull() {
        Specification<AirportEntity> spec = AirportSpecifications.nameOrIataContains(null);

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
    }

    @DisplayName("Should return all airports when search is blank")
    @Test
    void shouldReturnAllWhenSearchIsBlank() {
        Specification<AirportEntity> spec = AirportSpecifications.nameOrIataContains("   ");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
    }

    @DisplayName("Should combine country ISO code and search filters")
    @Test
    void shouldCombineFilters() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode("CO")
                .and(AirportSpecifications.nameOrIataContains("YYY"));

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIataCode()).isEqualTo("YYY");
    }

    @DisplayName("Should return empty when no airports match")
    @Test
    void shouldReturnEmptyWhenNoMatch() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode("FR");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @DisplayName("Should be case-insensitive when filtering by country ISO code")
    @Test
    void shouldBeCaseInsensitiveForIsoCode() {
        Specification<AirportEntity> spec = AirportSpecifications.hasCountryIsoCode("co");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(a -> a.getIataCode().equals("ZZZ"));
        assertThat(result).anyMatch(a -> a.getIataCode().equals("YYY"));
    }

    @DisplayName("Should be case-insensitive when searching by name")
    @Test
    void shouldBeCaseInsensitiveForSearch() {
        Specification<AirportEntity> spec = AirportSpecifications.nameOrIataContains("test airport one");

        List<AirportEntity> result = airportRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIataCode()).isEqualTo("ZZZ");
    }
}