package com.falcon.booking.feature.country.mapper;

import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CountryMapperTest {

    @InjectMocks
    private CountryMapperImpl countryMapper;

    private CountryEntity createCountryEntity() {
        CountryEntity entity = new CountryEntity();
        entity.setId(1);
        entity.setName("Colombia");
        entity.setIsoCode("CO");
        return entity;
    }

    @DisplayName("Should map country entity to CountryDto")
    @Test
    void shouldMapToDto() {
        CountryEntity entity = createCountryEntity();

        CountryDto result = countryMapper.toDto(entity);

        assertThat(result.name()).isEqualTo("Colombia");
        assertThat(result.isoCode()).isEqualTo("CO");
    }

    @DisplayName("Should map entity list to CountryDto list")
    @Test
    void shouldMapListToDto() {
        CountryEntity colombia = createCountryEntity();
        CountryEntity usa = new CountryEntity();
        usa.setId(2);
        usa.setName("United States");
        usa.setIsoCode("US");

        List<CountryDto> result = countryMapper.toDto(List.of(colombia, usa));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Colombia");
        assertThat(result.get(1).name()).isEqualTo("United States");
    }
}
