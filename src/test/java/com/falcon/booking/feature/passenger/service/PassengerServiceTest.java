package com.falcon.booking.feature.passenger.service;

import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.feature.country.service.CountryService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.exception.*;
import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PassengerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private PassengerMapper passengerMapper;
    @Mock
    private CountryService countryService;

    @InjectMocks
    private PassengerService passengerService;

    private CountryEntity createCountry(String isoCode) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setIsoCode(isoCode);
        countryEntity.setName("Country " + isoCode);
        return countryEntity;
    }

    private PassengerEntity createPassenger(String identificationNumber, String passportNumber, CountryEntity country) {
        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFirstName("Juan");
        passengerEntity.setLastName("Perez");
        passengerEntity.setGender(PassengerGender.M);
        passengerEntity.setDateOfBirth(LocalDate.of(1990, 1, 10));
        passengerEntity.setIdentificationNumber(identificationNumber);
        passengerEntity.setPassportNumber(passportNumber);
        passengerEntity.setCountryNationality(country);
        return passengerEntity;
    }

    private AddPassengerDto createAddDto(String passportNumber) {
        return new AddPassengerDto(
                "Juan",
                "Perez",
                PassengerGender.M,
                "CO",
                LocalDate.of(1990, 1, 10),
                passportNumber,
                "10001"
        );
    }

    @DisplayName("Should return passenger entity by id")
    @Test
    void shouldReturnEntity_getPassengerEntityById() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        given(passengerRepository.findById(1L)).willReturn(Optional.of(passengerEntity));

        PassengerEntity result = passengerService.getPassengerEntityById(1L);

        assertThat(result).isEqualTo(passengerEntity);
        verify(passengerRepository).findById(1L);
    }

    @DisplayName("Should throw exception when passenger id does not exist")
    @Test
    void shouldThrowException_getPassengerEntityById() {
        given(passengerRepository.findById(1L)).willReturn(Optional.empty());

        PassengerNotFoundException exception =
                assertThrows(PassengerNotFoundException.class, () -> passengerService.getPassengerEntityById(1L));

        assertThat(exception.getMessage()).contains("id 1");
        verify(passengerRepository).findById(1L);
    }

    @DisplayName("Should return passenger entity by passport with normalized value")
    @Test
    void shouldReturnEntity_getPassengerEntityByPassportNumber() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        given(passengerRepository.findByPassportNumber("AB1234")).willReturn(Optional.of(passengerEntity));

        PassengerEntity result = passengerService.getPassengerEntityByPassportNumber(" ab1234 ");

        assertThat(result).isEqualTo(passengerEntity);
        verify(passengerRepository).findByPassportNumber("AB1234");
    }

    @DisplayName("Should return passenger entity by identification and country")
    @Test
    void shouldReturnEntity_getPassengerEntityByIdentificationNumber() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        given(countryService.getCountryEntityByIsoCode(" co ")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(passengerEntity));

        PassengerEntity result = passengerService.getPassengerEntityByIdentificationNumber(" 10001 ", " co ");

        assertThat(result).isEqualTo(passengerEntity);
        verify(passengerRepository).findByIdentificationNumberAndCountryNationality("10001", country);
    }

    @DisplayName("Should create new passenger when it does not exist")
    @Test
    void shouldCreatePassenger_createOrGetPassenger() {
        CountryEntity country = createCountry("CO");
        AddPassengerDto addPassengerDto = createAddDto("AB1234");
        PassengerEntity mappedEntity = createPassenger("10001", "AB1234", null);
        PassengerEntity savedEntity = createPassenger("10001", "AB1234", country);
        savedEntity.setId(10L);

        given(passengerMapper.toEntity(addPassengerDto)).willReturn(mappedEntity);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.empty());
        given(passengerRepository.findByPassportNumber("AB1234")).willReturn(Optional.empty());
        given(passengerRepository.save(mappedEntity)).willReturn(savedEntity);

        PassengerEntity result = passengerService.createOrGetPassenger(addPassengerDto);

        assertThat(result).isEqualTo(savedEntity);
        verify(passengerRepository).save(mappedEntity);
    }

    @DisplayName("Should update existing passenger when identification already exists")
    @Test
    void shouldUpdatePassenger_createOrGetPassenger() {
        CountryEntity country = createCountry("CO");
        AddPassengerDto addPassengerDto = createAddDto("AB1234");
        PassengerEntity oldPassenger = createPassenger("10001", "AB1234", country);
        oldPassenger.setFirstName("CARLOS");
        PassengerEntity mappedEntity = createPassenger("10001", "AB1234", null);
        PassengerEntity savedEntity = createPassenger("10001", "AB1234", country);

        given(passengerMapper.toEntity(addPassengerDto)).willReturn(mappedEntity);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(oldPassenger));
        given(passengerRepository.findByPassportNumber("AB1234")).willReturn(Optional.of(oldPassenger));
        given(passengerRepository.save(oldPassenger)).willReturn(savedEntity);

        PassengerEntity result = passengerService.createOrGetPassenger(addPassengerDto);

        assertThat(result).isEqualTo(savedEntity);
        assertThat(oldPassenger.getFirstName()).isEqualTo("JUAN");
        verify(passengerRepository).save(oldPassenger);
    }

    @DisplayName("Should throw exception when passport belongs to another passenger")
    @Test
    void shouldThrowException_createOrGetPassenger_whenPassportAlreadyExists() {
        CountryEntity country = createCountry("CO");
        AddPassengerDto addPassengerDto = createAddDto("AB1234");
        PassengerEntity mappedEntity = createPassenger("10001", "AB1234", null);
        PassengerEntity anotherPassenger = createPassenger("99999", "AB1234", country);

        given(passengerMapper.toEntity(addPassengerDto)).willReturn(mappedEntity);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.empty());
        given(passengerRepository.findByPassportNumber("AB1234")).willReturn(Optional.of(anotherPassenger));

        PassengerAlreadyExistsException exception =
                assertThrows(PassengerAlreadyExistsException.class,
                        () -> passengerService.createOrGetPassenger(addPassengerDto));

        assertThat(exception.getMessage()).contains("AB1234");
        verify(passengerRepository, never()).save(mappedEntity);
    }

    @DisplayName("Should throw exception when existing passenger has different passport")
    @Test
    void shouldThrowException_createOrGetPassenger_whenExistingPassengerHasDifferentPassport() {
        CountryEntity country = createCountry("CO");
        AddPassengerDto addPassengerDto = createAddDto("AB9999");
        PassengerEntity mappedEntity = createPassenger("10001", "AB9999", null);
        PassengerEntity oldPassenger = createPassenger("10001", "AB1234", country);

        given(passengerMapper.toEntity(addPassengerDto)).willReturn(mappedEntity);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(oldPassenger));
        given(passengerRepository.findByPassportNumber("AB9999")).willReturn(Optional.empty());

        PassengerHasDifferentPassportNumberException exception =
                assertThrows(PassengerHasDifferentPassportNumberException.class,
                        () -> passengerService.createOrGetPassenger(addPassengerDto));

        assertThat(exception.getMessage()).contains("different registered passport");
        verify(passengerRepository, never()).save(oldPassenger);
    }

    @DisplayName("Should return dto without changing passport when it is the same")
    @Test
    void shouldReturnDto_updatePassengerPassport_whenSamePassport() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responsePassengerDto = new ResponsePassengerDto(1L, "JUAN", "PEREZ", PassengerGender.M,
                "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(passengerEntity));
        given(passengerRepository.save(passengerEntity)).willReturn(passengerEntity);
        given(passengerMapper.toResponseDto(passengerEntity)).willReturn(responsePassengerDto);

        ResponsePassengerDto result = passengerService.updatePassengerPassport("10001", "CO", "AB1234");

        assertThat(result).isEqualTo(responsePassengerDto);
        verify(passengerRepository).save(passengerEntity);
    }

    @DisplayName("Should throw exception when new passport already exists")
    @Test
    void shouldThrowException_updatePassengerPassport_whenPassportAlreadyExists() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);

        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(passengerEntity));
        given(passengerRepository.existsByPassportNumber("AB9999")).willReturn(true);

        PassengerAlreadyExistsException exception =
                assertThrows(PassengerAlreadyExistsException.class,
                        () -> passengerService.updatePassengerPassport("10001", "CO", "AB9999"));

        assertThat(exception.getMessage()).contains("AB9999");
    }

    @DisplayName("Should update passenger passport and return dto")
    @Test
    void shouldUpdatePassport_updatePassengerPassport() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responsePassengerDto = new ResponsePassengerDto(1L, "JUAN", "PEREZ", PassengerGender.M,
                "CO", LocalDate.of(1990, 1, 10), "AB9999", "10001");

        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.of(passengerEntity));
        given(passengerRepository.existsByPassportNumber("AB9999")).willReturn(false);
        given(passengerMapper.toResponseDto(passengerEntity)).willReturn(responsePassengerDto);

        ResponsePassengerDto result = passengerService.updatePassengerPassport("10001", "CO", "AB9999");

        assertThat(passengerEntity.getPassportNumber()).isEqualTo("AB9999");
        assertThat(result).isEqualTo(responsePassengerDto);
        verify(passengerRepository, never()).save(passengerEntity);
    }

    @DisplayName("Should return paginated page of all passengers sorted by name")
    @Test
    void shouldReturnPagedPassengers_getAllPassengers() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responseDto = new ResponsePassengerDto(1L, "Juan", "Perez",
                PassengerGender.M, "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        Page<PassengerEntity> entityPage = new PageImpl<>(List.of(passengerEntity),
                PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName"))), 1);
        given(passengerRepository.findAll(any(PageRequest.class))).willReturn(entityPage);
        given(passengerMapper.toResponseDto(passengerEntity)).willReturn(responseDto);

        Page<ResponsePassengerDto> result = passengerService.getAllPassengers(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).firstName()).isEqualTo("Juan");
    }

    @DisplayName("Should return paginated page of passengers filtered by flight")
    @Test
    void shouldReturnPagedPassengers_getPassengersByFlightId() {
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responseDto = new ResponsePassengerDto(1L, "Juan", "Perez",
                PassengerGender.M, "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        Page<PassengerEntity> entityPage = new PageImpl<>(List.of(passengerEntity),
                PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName"))), 1);
        given(passengerRepository.findDistinctByPassengerReservationsFlightId(any(Long.class), any(PageRequest.class)))
                .willReturn(entityPage);
        given(passengerMapper.toResponseDto(passengerEntity)).willReturn(responseDto);

        Page<ResponsePassengerDto> result = passengerService.getPassengersByFlightId(5L, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).identificationNumber()).isEqualTo("10001");
    }

    @DisplayName("Should return my profile when user has linked passenger")
    @Test
    void shouldReturnMyProfile_getMyProfile() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        CountryEntity country = createCountry("CO");
        PassengerEntity passengerEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responseDto = new ResponsePassengerDto(1L, "JUAN", "PEREZ",
                PassengerGender.M, "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        given(passengerRepository.findByUser(user)).willReturn(Optional.of(passengerEntity));
        given(passengerMapper.toResponseDto(passengerEntity)).willReturn(responseDto);

        ResponsePassengerDto result = passengerService.getMyProfile(user);

        assertThat(result).isEqualTo(responseDto);
        verify(passengerRepository).findByUser(user);
    }

    @DisplayName("Should throw exception when getting my profile without linked passenger")
    @Test
    void shouldThrowException_getMyProfile_notFound() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        given(passengerRepository.findByUser(user)).willReturn(Optional.empty());

        assertThrows(PassengerProfileNotFoundException.class,
                () -> passengerService.getMyProfile(user));
    }

    @DisplayName("Should create and link passenger profile")
    @Test
    void shouldCreateMyProfile() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        CountryEntity country = createCountry("CO");
        AddPassengerDto addDto = createAddDto("AB1234");
        PassengerEntity mappedEntity = createPassenger("10001", "AB1234", null);
        PassengerEntity savedEntity = createPassenger("10001", "AB1234", country);
        savedEntity.setUser(user);
        ResponsePassengerDto responseDto = new ResponsePassengerDto(1L, "JUAN", "PEREZ",
                PassengerGender.M, "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        given(passengerRepository.existsByUser(user)).willReturn(false);
        given(passengerMapper.toEntity(addDto)).willReturn(mappedEntity);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.findByIdentificationNumberAndCountryNationality("10001", country))
                .willReturn(Optional.empty());
        given(passengerRepository.findByPassportNumber("AB1234")).willReturn(Optional.empty());
        given(passengerRepository.save(any(PassengerEntity.class))).willReturn(savedEntity);
        given(passengerMapper.toResponseDto(savedEntity)).willReturn(responseDto);

        ResponsePassengerDto result = passengerService.createMyProfile(user, addDto);

        assertThat(result).isEqualTo(responseDto);
        verify(passengerRepository).existsByUser(user);
        verify(passengerRepository, times(2)).save(any(PassengerEntity.class));
    }

    @DisplayName("Should throw exception when creating profile that already exists")
    @Test
    void shouldThrowException_createMyProfile_alreadyLinked() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        AddPassengerDto addDto = createAddDto("AB1234");

        given(passengerRepository.existsByUser(user)).willReturn(true);

        assertThrows(PassengerProfileAlreadyLinkedException.class,
                () -> passengerService.createMyProfile(user, addDto));
        verify(passengerRepository, never()).save(any());
    }

    @DisplayName("Should update existing passenger profile")
    @Test
    void shouldUpdateMyProfile() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        CountryEntity country = createCountry("CO");
        PassengerEntity existingPassenger = createPassenger("10001", "AB1234", country);
        AddPassengerDto updateDto = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1990, 1, 10), "AB1234", "10001");
        PassengerEntity savedEntity = createPassenger("10001", "AB1234", country);
        ResponsePassengerDto responseDto = new ResponsePassengerDto(1L, "JUAN", "PEREZ",
                PassengerGender.M, "CO", LocalDate.of(1990, 1, 10), "AB1234", "10001");

        given(passengerRepository.findByUser(user)).willReturn(Optional.of(existingPassenger));
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(passengerRepository.save(existingPassenger)).willReturn(savedEntity);
        given(passengerMapper.toResponseDto(savedEntity)).willReturn(responseDto);

        ResponsePassengerDto result = passengerService.updateMyProfile(user, updateDto);

        assertThat(result).isEqualTo(responseDto);
        verify(passengerRepository).findByUser(user);
        verify(passengerRepository).save(existingPassenger);
    }

    @DisplayName("Should throw exception when updating profile without linked passenger")
    @Test
    void shouldThrowException_updateMyProfile_notFound() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        AddPassengerDto updateDto = createAddDto("AB1234");

        given(passengerRepository.findByUser(user)).willReturn(Optional.empty());

        assertThrows(PassengerProfileNotFoundException.class,
                () -> passengerService.updateMyProfile(user, updateDto));
    }
}
