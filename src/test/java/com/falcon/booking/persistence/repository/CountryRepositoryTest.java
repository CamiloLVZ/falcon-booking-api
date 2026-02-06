package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.CountryEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("tests")
public class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void shouldReturnCountry_whenIsoCodeExists(){
        CountryEntity country = new CountryEntity();
        country.setIsoCode("CO");
        country.setName("Colombia");
        countryRepository.save(country);


        Optional<CountryEntity> countryFound = countryRepository.findByIsoCode("CO");


        Assertions.assertThat(countryFound).isPresent();
        CountryEntity result = countryFound.get();
        Assertions.assertThat(result.getIsoCode()).isEqualTo(country.getIsoCode());
        Assertions.assertThat(result.getName()).isEqualTo(country.getName());
    }

    @Test
    void shouldReturnEmptyOptional_whenIsoCodeDoesNotExist(){
        CountryEntity country = new CountryEntity();
        country.setIsoCode("CO");
        country.setName("Colombia");
        countryRepository.save(country);


        Optional<CountryEntity> countryFound = countryRepository.findByIsoCode("US");


        Assertions.assertThat(countryFound).isEmpty();
    }
}
