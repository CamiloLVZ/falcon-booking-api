package com.falcon.booking.feature.country.service;

import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.dto.CreateCountryDto;
import com.falcon.booking.feature.country.exception.CountryAlreadyExistsException;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import com.falcon.booking.feature.country.mapper.CountryMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.CountryRepository;
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

        CountryNotFoundException ex =
                assertThrows(CountryNotFoundException.class,
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

        CountryNotFoundException ex =
                assertThrows(CountryNotFoundException.class,
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
        given(countryRepository.findAllByOrderByNameAsc()).willReturn(countryList);
        given(countryMapper.toDto(countryList)).willReturn(expectedDtoList);

        List<CountryDto> listFound = countryService.getAllCountries();

        verify(countryRepository).findAllByOrderByNameAsc();
        verify(countryMapper).toDto(countryList);
        assertThat(listFound).isEqualTo(expectedDtoList);
    }

    @DisplayName("Should return empty CountryDto list when there is no countries")
    @Test
    void shouldReturnEmptyDtoList_getAllCountries(){
        given(countryRepository.findAllByOrderByNameAsc()).willReturn(List.of());
        given(countryMapper.toDto(List.of())).willReturn(List.of());

        List<CountryDto> listFound = countryService.getAllCountries();

        verify(countryRepository).findAllByOrderByNameAsc();
        verify(countryMapper).toDto(List.of());
        assertThat(listFound).isNotNull();
        assertThat(listFound).isEmpty();
    }

    @DisplayName("Should create country and return CountryDto")
    @Test
    void shouldCreateCountry() {
        CreateCountryDto dto = new CreateCountryDto("Colombia", "CO");
        CountryEntity entity = new CountryEntity();
        entity.setName("Colombia");
        entity.setIsoCode("CO");
        CountryEntity saved = new CountryEntity();
        saved.setId(1);
        saved.setName("Colombia");
        saved.setIsoCode("CO");
        CountryDto expectedDto = new CountryDto("Colombia", "CO");

        given(countryRepository.findByIsoCode("CO")).willReturn(Optional.empty());
        given(countryRepository.existsByNameIgnoreCase("Colombia")).willReturn(false);
        given(countryMapper.toEntity(dto)).willReturn(entity);
        given(countryRepository.save(entity)).willReturn(saved);
        given(countryMapper.toDto(saved)).willReturn(expectedDto);

        CountryDto result = countryService.createCountry(dto);

        assertThat(result).isEqualTo(expectedDto);
        verify(countryRepository).findByIsoCode("CO");
        verify(countryRepository).existsByNameIgnoreCase("Colombia");
        verify(countryMapper).toEntity(dto);
        verify(countryRepository).save(entity);
        verify(countryMapper).toDto(saved);
    }

    @DisplayName("Should throw exception when country ISO code already exists")
    @Test
    void shouldThrowException_createCountry_duplicateIsoCode() {
        CreateCountryDto dto = new CreateCountryDto("Colombia", "CO");
        CountryEntity existing = new CountryEntity();
        existing.setName("Colombia");
        existing.setIsoCode("CO");

        given(countryRepository.findByIsoCode("CO")).willReturn(Optional.of(existing));

        CountryAlreadyExistsException ex =
                assertThrows(CountryAlreadyExistsException.class,
                        () -> countryService.createCountry(dto));

        assertThat(ex.getMessage()).contains("ISO code");
        verify(countryRepository).findByIsoCode("CO");
    }

    @DisplayName("Should throw exception when country name already exists")
    @Test
    void shouldThrowException_createCountry_duplicateName() {
        CreateCountryDto dto = new CreateCountryDto("Colombia", "CO");

        given(countryRepository.findByIsoCode("CO")).willReturn(Optional.empty());
        given(countryRepository.existsByNameIgnoreCase("Colombia")).willReturn(true);

        CountryAlreadyExistsException ex =
                assertThrows(CountryAlreadyExistsException.class,
                        () -> countryService.createCountry(dto));

        assertThat(ex.getMessage()).contains("name");
        verify(countryRepository).findByIsoCode("CO");
        verify(countryRepository).existsByNameIgnoreCase("Colombia");
    }

    @DisplayName("Should normalize ISO code when creating country")
    @Test
    void shouldNormalizeIsoCode_createCountry() {
        CreateCountryDto dto = new CreateCountryDto("Colombia", " co ");
        CountryEntity entity = new CountryEntity();
        entity.setName("Colombia");
        entity.setIsoCode("CO");
        CountryEntity saved = new CountryEntity();
        saved.setId(1);
        saved.setName("Colombia");
        saved.setIsoCode("CO");
        CountryDto expectedDto = new CountryDto("Colombia", "CO");

        given(countryRepository.findByIsoCode("CO")).willReturn(Optional.empty());
        given(countryRepository.existsByNameIgnoreCase("Colombia")).willReturn(false);
        given(countryMapper.toEntity(dto)).willReturn(entity);
        given(countryRepository.save(entity)).willReturn(saved);
        given(countryMapper.toDto(saved)).willReturn(expectedDto);

        CountryDto result = countryService.createCountry(dto);

        assertThat(result).isEqualTo(expectedDto);
        verify(countryRepository).findByIsoCode("CO");
        verify(countryRepository).existsByNameIgnoreCase("Colombia");
    }
}
