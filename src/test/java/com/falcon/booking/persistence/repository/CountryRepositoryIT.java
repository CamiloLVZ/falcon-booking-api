package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.CountryEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Optional;

public class CountryRepositoryIT extends BaseRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void shouldReturnCountry_whenIsoCodeExists(){
        Optional<CountryEntity> countryFound = countryRepository.findByIsoCode("CO");

        Assertions.assertThat(countryFound).isPresent();
        CountryEntity result = countryFound.get();
        Assertions.assertThat(result.getIsoCode()).isEqualTo("CO");
        Assertions.assertThat(result.getName()).isEqualTo("Colombia");
    }

    @Test
    void shouldReturnEmptyOptional_whenIsoCodeDoesNotExist(){
        Optional<CountryEntity> countryFound = countryRepository.findByIsoCode("XX");

        Assertions.assertThat(countryFound).isEmpty();
    }
}
