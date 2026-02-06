package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.CountryDoesNotExistException;
import com.falcon.booking.domain.mapper.CountryMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.CountryRepository;
import com.falcon.booking.web.dto.CountryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;
    @Mock
    private CountryMapper countryMapper;

    @InjectMocks
    private CountryService countryService;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setIsoCode(isoCode);
        countryEntity.setName(name);
        return countryEntity;
    }

    @DisplayName("Should return Country entity with a existing iso code ")
    @Test
    void shouldReturnEntity_getCountryEntityByIsoCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        given(countryRepository.findByIsoCode("CO"))
                .willReturn(Optional.of(country));


        CountryEntity countryFound = countryService.getCountryEntityByIsoCode(" co ");

        assertThat(countryFound).isNotNull();
        assertThat(countryFound).isEqualTo(country);
        verify(countryRepository).findByIsoCode("CO");
    }

    @DisplayName("Should throw exception when country entity does not exist")
    @Test
    void shouldThrowException_getCountryEntityByIsoCode() {
        given(countryRepository.findByIsoCode("US"))
                .willReturn(Optional.empty());

        CountryDoesNotExistException ex =
                assertThrows(CountryDoesNotExistException.class,
                        ()-> countryService.getCountryEntityByIsoCode(" us "));

        assertThat(ex.getMessage()).contains("us");
       verify(countryRepository).findByIsoCode("US");
    }

    @DisplayName("Should return CountryDto when country exists")
    @Test
    void shouldReturnDto_getCountryByIsoCode() {
        CountryEntity country = createCountry("US", "United States");
        CountryDto expectedDto = new CountryDto("United States", "US");
        given(countryRepository.findByIsoCode("US"))
                .willReturn(Optional.of(country));
        given(countryMapper.toDto(country))
                .willReturn(expectedDto);

        CountryDto countryFoundDto = countryService.getCountryByIsoCode("us ");

        verify(countryRepository).findByIsoCode("US");
        verify(countryMapper).toDto(country);
        assertThat(countryFoundDto).isNotNull();
        assertThat(countryFoundDto).isEqualTo(expectedDto);
    }

    @DisplayName("Should throw exception when country does not exist in getCountryByIsoCode")
    @Test
    void shouldThrowException_getCountryByIsoCode() {
        given(countryRepository.findByIsoCode("US"))
                .willReturn(Optional.empty());

        CountryDoesNotExistException ex =
                assertThrows(CountryDoesNotExistException.class,
                        ()-> countryService.getCountryByIsoCode(" us "));

        assertThat(ex.getMessage()).contains("us");
        verify(countryRepository).findByIsoCode("US");
    }

    @DisplayName("Should return CountryDto list when getAllCountries is called")
    @Test
    void shouldReturnDtoList_getAllCountries(){
        CountryEntity country1 = createCountry("US", "United States");
        CountryEntity country2 = createCountry("CA", "Canada");
        CountryEntity country3 = createCountry("MX", "Mexico");
        List<CountryEntity> countryList = List.of(country1, country2, country3);
        CountryDto expectedDto1 = new CountryDto("United States", "US");
        CountryDto expectedDto2 = new CountryDto("Canada", "CA");
        CountryDto expectedDto3 = new CountryDto("MX", "Mexico");
        List<CountryDto> expectedDtoList = List.of(expectedDto1, expectedDto2, expectedDto3);
        given(countryRepository.findAll()).willReturn(countryList);
        given(countryMapper.toDto(countryList)).willReturn(expectedDtoList);

        List<CountryDto> listFound = countryService.getAllCountries();

        verify(countryRepository).findAll();
        verify(countryMapper).toDto(countryList);
        assertThat(listFound).isEqualTo(expectedDtoList);
    }

    @DisplayName("Should return empty CountryDto list when there is no countries")
    @Test
    void shouldReturnEmptyDtoList_getAllCountries(){
        given(countryRepository.findAll()).willReturn(List.of());
        given(countryMapper.toDto(List.of())).willReturn(List.of());

        List<CountryDto> listFound = countryService.getAllCountries();

        verify(countryRepository).findAll();
        verify(countryMapper).toDto(List.of());
        assertThat(listFound).isNotNull();
        assertThat(listFound).isEmpty();
    }
}
