package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.Passenger.PassengerAlreadyExistsException;
import com.falcon.booking.domain.exception.Passenger.PassengerDoesNotExistException;
import com.falcon.booking.domain.exception.Passenger.PassengerHasDifferentPassportNumberException;
import com.falcon.booking.domain.mapper.PassengerMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.repository.PassengerRepository;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PassengerEntity getPassengerEntityById(Long id){
        return passengerRepository.findById(id).orElseThrow(
                ()->new PassengerDoesNotExistException(id));
    }

    public PassengerEntity getPassengerEntityByPassportNumber(String passportNumber){
        String normalizedPassportNumber = StringNormalizer.normalize(passportNumber);
        return passengerRepository.findByPassportNumber(normalizedPassportNumber).orElseThrow(
                ()->new PassengerDoesNotExistException(passportNumber));
    }

    public PassengerEntity getPassengerEntityByIdentificationNumber(String identificationNumber, String nationalityIsoCode){
        String normalizedIdentificationNumber = StringNormalizer.normalize(identificationNumber);
        CountryEntity country = countryService.getCountryEntityByIsoCode(nationalityIsoCode);
        
        return passengerRepository.findByIdentificationNumberAndCountryNationality(normalizedIdentificationNumber, country).orElseThrow(
                ()->new PassengerDoesNotExistException(identificationNumber, country.getIsoCode()));
    }

    @Transactional
    public ResponsePassengerDto addPassenger(AddPassengerDto addPassengerDto){
        return passengerMapper.toResponseDto(createOrGetPassenger(addPassengerDto));
    }

    @Transactional
    public PassengerEntity createOrGetPassenger(AddPassengerDto addPassengerDto){
        PassengerEntity newPassengerEntity = passengerMapper.toEntity(addPassengerDto);
        CountryEntity country = countryService.getCountryEntityByIsoCode(addPassengerDto.nationalityIsoCode());
        newPassengerEntity.setCountryNationality(country);

        PassengerEntity oldPassengerEntity =
                passengerRepository.findByIdentificationNumberAndCountryNationality(newPassengerEntity.getIdentificationNumber(), newPassengerEntity.getCountryNationality()).orElse(null);

        if(newPassengerEntity.getPassportNumber()!=null){
            validatePassportNumber(oldPassengerEntity, newPassengerEntity);
        }
        if(oldPassengerEntity != null){
            oldPassengerEntity.setFirstName(newPassengerEntity.getFirstName());
            oldPassengerEntity.setLastName(newPassengerEntity.getLastName());
            oldPassengerEntity.setGender(newPassengerEntity.getGender());
            oldPassengerEntity.setDateOfBirth(newPassengerEntity.getDateOfBirth());
            return passengerRepository.save(oldPassengerEntity);
        }else {
            return passengerRepository.save(newPassengerEntity);
        }

    }

    private void validatePassportNumber(PassengerEntity oldPassengerEntity, PassengerEntity newPassengerEntity){
        PassengerEntity oldPassengerByPassport = passengerRepository.findByPassportNumber(newPassengerEntity.getPassportNumber()).orElse(null);

        if(oldPassengerByPassport != null){
            if(!oldPassengerByPassport.equals(oldPassengerEntity)){
                throw new PassengerAlreadyExistsException(newPassengerEntity.getPassportNumber());
            }
        }else{
            if(oldPassengerEntity != null){
                if(!newPassengerEntity.getPassportNumber().equals(oldPassengerEntity.getPassportNumber())){
                    throw new PassengerHasDifferentPassportNumberException();
                }
            }
        }
    }

    @Transactional
    public ResponsePassengerDto updatePassengerPassport(String identificationNumber, String nationalityIsoCode, String newPassportNumber){
        PassengerEntity passengerEntity = getPassengerEntityByIdentificationNumber(identificationNumber, nationalityIsoCode);
        if(newPassportNumber.equals(passengerEntity.getPassportNumber())){
            return passengerMapper.toResponseDto(passengerRepository.save(passengerEntity));
        }

        boolean existPassengerWithPassport = passengerRepository.existsByPassportNumber(newPassportNumber);
        if(existPassengerWithPassport){
            throw new PassengerAlreadyExistsException(newPassportNumber);
        }else{
            passengerEntity.setPassportNumber(newPassportNumber);
            return passengerMapper.toResponseDto(passengerEntity);
        }
    }

    public ResponsePassengerDto getPassengerById(Long id){
        return passengerMapper.toResponseDto(getPassengerEntityById(id));
    }

    public ResponsePassengerDto getPassengerByPassportNumber(String passportNumber){
        return passengerMapper.toResponseDto(getPassengerEntityByPassportNumber(passportNumber));
    }

    public ResponsePassengerDto getPassengerByIdentificationNumber(String identificationNumber, String nationalityIsoCode){
        return passengerMapper.toResponseDto(getPassengerEntityByIdentificationNumber(identificationNumber, nationalityIsoCode));
    }



}
