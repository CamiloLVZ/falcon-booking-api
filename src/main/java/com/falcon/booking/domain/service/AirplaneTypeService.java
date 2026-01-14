package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.exception.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirplaneTypeStatusInvalidException;
import com.falcon.booking.domain.exception.InvalidSearchCriteriaException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.web.dto.AirplaneTypeDto.AirplaneTypeResponseDto;
import com.falcon.booking.web.dto.AirplaneTypeDto.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.AirplaneTypeDto.UpdateAirplaneTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AirplaneTypeService {

    private final AirplaneTypeRepository airplaneTypeRepository;
    private final AirplaneTypeMapper airplaneTypeMapper;

    @Autowired
    public AirplaneTypeService(AirplaneTypeRepository airplaneTypeRepository, AirplaneTypeMapper airplaneTypeMapper) {
        this.airplaneTypeRepository = airplaneTypeRepository;
        this.airplaneTypeMapper = airplaneTypeMapper;
    }

    public AirplaneTypeResponseDto getAirplaneTypeById(Long id) {
        AirplaneTypeEntity airplaneTypeEntity = airplaneTypeRepository.findById(id).
                orElseThrow(() -> new AirplaneTypeDoesNotExistException(id));
        return airplaneTypeMapper.toResponseDto(airplaneTypeEntity);
    }

    public List<AirplaneTypeResponseDto> getAirplaneTypes(String producer, String model, AirplaneTypeStatus status) {

        if (model != null && producer == null) {
            throw new InvalidSearchCriteriaException(
                    "Searching by model alone is not supported"
            );
        }

        if (producer != null && model != null) {
            AirplaneTypeEntity entity =
                    airplaneTypeRepository.findByProducerIgnoreCaseAndModelContainingIgnoreCase(producer, model)
                            .orElseThrow(() -> new AirplaneTypeDoesNotExistException(producer, model));
            return List.of(airplaneTypeMapper.toResponseDto(entity));
        }

        if (producer != null) {
            List<AirplaneTypeResponseDto> list;
            if(status==null){
            list =
                    airplaneTypeMapper.toResponseDto(airplaneTypeRepository.findAllByProducerIgnoreCase(producer));

            return list;}
            else{
                list=airplaneTypeMapper.toResponseDto(
                        airplaneTypeRepository.findAllByProducerIgnoreCaseAndStatus(producer,status));
                return list;}
        }
        if (status != null) {
            return airplaneTypeMapper.toResponseDto(
                    airplaneTypeRepository.findAllByStatus(status));
        }
        return airplaneTypeMapper.toResponseDto(
                airplaneTypeRepository.findAll());
    }

    public List<AirplaneTypeResponseDto> getAllAirplaneTypes() {
        List<AirplaneTypeEntity> airplaneTypeEntities = airplaneTypeRepository.findAll();
        return airplaneTypeMapper.toResponseDto(airplaneTypeEntities);
    }

    public AirplaneTypeResponseDto addAirplaneType(CreateAirplaneTypeDto createAirplaneTypeDto) {
        String producer = createAirplaneTypeDto.producer();
        String model = createAirplaneTypeDto.model();

        boolean exists = airplaneTypeRepository.existsByProducerAndModel(producer, model);
        if(exists) throw new AirplaneTypeAlreadyExistsException(producer, model);

        AirplaneTypeEntity entityToSave = airplaneTypeMapper.toEntity(createAirplaneTypeDto);
        entityToSave.setStatus(AirplaneTypeStatus.ACTIVE);
        AirplaneTypeEntity entityCreated = airplaneTypeRepository.save(entityToSave);
        return airplaneTypeMapper.toResponseDto(entityCreated);
    }

    public AirplaneTypeResponseDto updateAirplaneType(Long id, UpdateAirplaneTypeDto updateAirplaneTypeDto) {

       AirplaneTypeEntity entityToUpdate = airplaneTypeRepository.findById(id).
               orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        if(updateAirplaneTypeDto.economySeats()!=null) entityToUpdate.setEconomySeats(updateAirplaneTypeDto.economySeats());
        if(updateAirplaneTypeDto.firstClassSeats()!=null) entityToUpdate.setFirstClassSeats(updateAirplaneTypeDto.firstClassSeats());
        if(updateAirplaneTypeDto.status()!=null) entityToUpdate.setStatus(AirplaneTypeStatus.valueOf(updateAirplaneTypeDto.status()));

        AirplaneTypeEntity updatedEntity = airplaneTypeRepository.save(entityToUpdate);

        return airplaneTypeMapper.toResponseDto(updatedEntity);
    }
}
