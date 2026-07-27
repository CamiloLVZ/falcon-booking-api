package com.falcon.booking.feature.passenger.service;

import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.country.service.CountryService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.exception.PassengerAlreadyExistsException;
import com.falcon.booking.feature.passenger.exception.PassengerHasDifferentPassportNumberException;
import com.falcon.booking.feature.passenger.exception.PassengerNotFoundException;
import com.falcon.booking.feature.passenger.exception.PassengerProfileAlreadyLinkedException;
import com.falcon.booking.feature.passenger.exception.PassengerProfileNotFoundException;
import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PassengerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;
    private final CountryService countryService;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository, PassengerMapper passengerMapper, CountryService countryService) {
        this.passengerRepository = passengerRepository;
        this.passengerMapper = passengerMapper;
        this.countryService = countryService;
    }

    public PassengerEntity getPassengerEntityById(Long id) {
        return passengerRepository.findById(id).orElseThrow(
                () -> new PassengerNotFoundException(id));
    }

    public PassengerEntity getPassengerEntityByPassportNumber(String passportNumber) {
        String normalizedPassportNumber = StringNormalizer.normalize(passportNumber);
        return passengerRepository.findByPassportNumber(normalizedPassportNumber).orElseThrow(
                () -> new PassengerNotFoundException(passportNumber));
    }

    public PassengerEntity getPassengerEntityByIdentificationNumber(String identificationNumber, String nationalityIsoCode) {
        String normalizedIdentificationNumber = StringNormalizer.normalize(identificationNumber);
        CountryEntity country = countryService.getCountryEntityByIsoCode(nationalityIsoCode);

        return passengerRepository.findByIdentificationNumberAndCountryNationality(normalizedIdentificationNumber, country).orElseThrow(
                () -> new PassengerNotFoundException(identificationNumber, country.getIsoCode()));
    }

    @Transactional
    public ResponsePassengerDto addPassenger(AddPassengerDto addPassengerDto) {
        return passengerMapper.toResponseDto(createOrGetPassenger(addPassengerDto));
    }

    public PassengerEntity createOrGetPassenger(AddPassengerDto addPassengerDto) {
        PassengerEntity newPassengerEntity = passengerMapper.toEntity(addPassengerDto);
        CountryEntity country = countryService.getCountryEntityByIsoCode(addPassengerDto.nationalityIsoCode());
        newPassengerEntity.setCountryNationality(country);

        PassengerEntity oldPassengerEntity =
                passengerRepository.findByIdentificationNumberAndCountryNationality(newPassengerEntity.getIdentificationNumber(), newPassengerEntity.getCountryNationality()).orElse(null);

        if (newPassengerEntity.getPassportNumber() != null) {
            validatePassportNumber(oldPassengerEntity, newPassengerEntity);
        }
        if (oldPassengerEntity != null) {
            oldPassengerEntity.setFirstName(newPassengerEntity.getFirstName());
            oldPassengerEntity.setLastName(newPassengerEntity.getLastName());
            oldPassengerEntity.setGender(newPassengerEntity.getGender());
            oldPassengerEntity.setDateOfBirth(newPassengerEntity.getDateOfBirth());
            if (newPassengerEntity.getPassportNumber() != null)
                oldPassengerEntity.setPassportNumber(newPassengerEntity.getPassportNumber());

            return passengerRepository.save(oldPassengerEntity);
        } else {
            PassengerEntity passengerCreated = passengerRepository.save(newPassengerEntity);
            log.info("Passenger created with id: {}", passengerCreated.getId());
            return passengerCreated;
        }
    }

    private void validatePassportNumber(PassengerEntity oldPassengerEntity, PassengerEntity newPassengerEntity) {
        PassengerEntity oldPassengerByPassport = passengerRepository.findByPassportNumber(newPassengerEntity.getPassportNumber()).orElse(null);

        if (oldPassengerByPassport != null) {
            if (!oldPassengerByPassport.equals(oldPassengerEntity)) {
                throw new PassengerAlreadyExistsException(newPassengerEntity.getPassportNumber());
            }
        } else {
            if (oldPassengerEntity != null) {
                if (oldPassengerEntity.getPassportNumber() == null)
                    return;

                if (!newPassengerEntity.getPassportNumber().equals(oldPassengerEntity.getPassportNumber())) {
                    throw new PassengerHasDifferentPassportNumberException();
                }
            }
        }
    }

    private void checkPassportNumberNotTaken(String newPassportNumber) {
        if (passengerRepository.existsByPassportNumber(newPassportNumber)) {
            throw new PassengerAlreadyExistsException(newPassportNumber);
        }
    }

    @Transactional
    public ResponsePassengerDto updatePassengerPassport(String identificationNumber, String nationalityIsoCode, String newPassportNumber) {
        PassengerEntity passengerEntity = getPassengerEntityByIdentificationNumber(identificationNumber, nationalityIsoCode);
        if (newPassportNumber.equals(passengerEntity.getPassportNumber())) {
            return passengerMapper.toResponseDto(passengerRepository.save(passengerEntity));
        }

        checkPassportNumberNotTaken(newPassportNumber);

        passengerEntity.setPassportNumber(newPassportNumber);
        log.info("Passenger {} updated passport number to {}", passengerEntity.getId(), newPassportNumber);
        return passengerMapper.toResponseDto(passengerEntity);
    }

    @Transactional(readOnly = true)
    public ResponsePassengerDto getPassengerById(Long id) {
        return passengerMapper.toResponseDto(getPassengerEntityById(id));
    }

    @Transactional(readOnly = true)
    public ResponsePassengerDto getPassengerByPassportNumber(String passportNumber) {
        return passengerMapper.toResponseDto(getPassengerEntityByPassportNumber(passportNumber));
    }

    @Transactional(readOnly = true)
    public ResponsePassengerDto getPassengerByIdentificationNumber(String identificationNumber, String nationalityIsoCode) {
        return passengerMapper.toResponseDto(getPassengerEntityByIdentificationNumber(identificationNumber, nationalityIsoCode));
    }

    @Transactional(readOnly = true)
    public Page<ResponsePassengerDto> getAllPassengers(int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName"));
        return passengerRepository.findAll(PageRequest.of(page, size, sort))
                .map(passengerMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ResponsePassengerDto> getPassengersByFlightId(Long flightId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName"));
        return passengerRepository.findDistinctByPassengerReservationsFlightId(flightId, PageRequest.of(page, size, sort))
                .map(passengerMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public ResponsePassengerDto getMyProfile(UserEntity user) {
        PassengerEntity passenger = passengerRepository.findByUser(user)
                .orElseThrow(PassengerProfileNotFoundException::new);
        return passengerMapper.toResponseDto(passenger);
    }

    @Transactional
    public ResponsePassengerDto createMyProfile(UserEntity user, AddPassengerDto dto) {
        if (passengerRepository.existsByUser(user)) {
            throw new PassengerProfileAlreadyLinkedException();
        }
        PassengerEntity passenger = createOrGetPassenger(dto);
        passenger.setUser(user);
        log.info("Passenger profile linked to user id: {}", user.getId());
        return passengerMapper.toResponseDto(passengerRepository.save(passenger));
    }

    @Transactional
    public ResponsePassengerDto updateMyProfile(UserEntity user, AddPassengerDto dto) {
        PassengerEntity passenger = passengerRepository.findByUser(user)
                .orElseThrow(PassengerProfileNotFoundException::new);

        CountryEntity country = countryService.getCountryEntityByIsoCode(dto.nationalityIsoCode());
        passenger.setFirstName(dto.firstName());
        passenger.setLastName(dto.lastName());
        passenger.setGender(dto.gender());
        passenger.setDateOfBirth(dto.dateOfBirth());
        passenger.setCountryNationality(country);
        if (dto.identificationNumber() != null) {
            passenger.setIdentificationNumber(dto.identificationNumber());
        }
        if (dto.passportNumber() != null) {
            passenger.setPassportNumber(dto.passportNumber());
        }
        log.info("Passenger profile updated for user id: {}", user.getId());
        return passengerMapper.toResponseDto(passengerRepository.save(passenger));
    }
}
