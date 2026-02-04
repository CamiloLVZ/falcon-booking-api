package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.specification.AirplaneTypeSpecifications;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AirplaneTypeEntity getAirplaneTypeEntity(Long id){
       return airplaneTypeRepository.findById(id).
                orElseThrow(() -> new AirplaneTypeDoesNotExistException(id));
    }

    @Transactional(readOnly = true)
    public ResponseAirplaneTypeDto getAirplaneTypeById(Long id) {
        AirplaneTypeEntity airplaneTypeEntity = getAirplaneTypeEntity(id);
        return airplaneTypeMapper.toResponseDto(airplaneTypeEntity);
    }

    @Transactional(readOnly = true)
    public List<ResponseAirplaneTypeDto> getAirplaneTypes(String producer, String model, AirplaneTypeStatus status) {

        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);

        Specification<AirplaneTypeEntity> specification = Specification.allOf();
        specification = specification.and(AirplaneTypeSpecifications.hasModel(model));
        specification = specification.and(AirplaneTypeSpecifications.hasProducer(producer));
        specification = specification.and(AirplaneTypeSpecifications.hasStatus(status));

        List<AirplaneTypeEntity> entites = airplaneTypeRepository.findAll(specification);

        return airplaneTypeMapper.toResponseDto(entites);
    }

    @Transactional
    public ResponseAirplaneTypeDto addAirplaneType(CreateAirplaneTypeDto createAirplaneTypeDto) {
        String producer = createAirplaneTypeDto.producer();
        String model = createAirplaneTypeDto.model();

        boolean exists = airplaneTypeRepository.existsByProducerAndModel(producer, model);
        if(exists) throw new AirplaneTypeAlreadyExistsException(producer, model);

        AirplaneTypeEntity entityToSave = airplaneTypeMapper.toEntity(createAirplaneTypeDto);
        entityToSave.setStatus(AirplaneTypeStatus.ACTIVE);
        AirplaneTypeEntity entityCreated = airplaneTypeRepository.save(entityToSave);
        return airplaneTypeMapper.toResponseDto(entityCreated);
    }

    @Transactional
    public ResponseAirplaneTypeDto updateAirplaneType(Long id, UpdateAirplaneTypeDto updateAirplaneTypeDto) {

       AirplaneTypeEntity entityToUpdate = airplaneTypeRepository.findById(id).
               orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        if(updateAirplaneTypeDto.economySeats()!=null) entityToUpdate.setEconomySeats(updateAirplaneTypeDto.economySeats());
        if(updateAirplaneTypeDto.firstClassSeats()!=null) entityToUpdate.setFirstClassSeats(updateAirplaneTypeDto.firstClassSeats());
        if(updateAirplaneTypeDto.status()!=null) entityToUpdate.setStatus(updateAirplaneTypeDto.status());

        AirplaneTypeEntity updatedEntity = airplaneTypeRepository.save(entityToUpdate);

        return airplaneTypeMapper.toResponseDto(updatedEntity);
    }

    @Transactional
    public ResponseAirplaneTypeDto correctAirplaneType(Long id, CorrectAirplaneTypeDto correctAirplaneTypeDto) {

        AirplaneTypeEntity entityToCorrect = airplaneTypeRepository.findById(id).
                orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        String producerToValidate= correctAirplaneTypeDto.producer() != null ? correctAirplaneTypeDto.producer(): entityToCorrect.getProducer();
        String modelToValidate= correctAirplaneTypeDto.model() != null ? correctAirplaneTypeDto.model(): entityToCorrect.getModel();

        boolean isChanging = !entityToCorrect.getModel().equals(modelToValidate)
                || !entityToCorrect.getProducer().equals(producerToValidate);

        if(!isChanging) return airplaneTypeMapper.toResponseDto(entityToCorrect);

        if(airplaneTypeRepository.existsByProducerAndModel(producerToValidate, modelToValidate)){
            throw new AirplaneTypeAlreadyExistsException(producerToValidate, modelToValidate);
        }

        entityToCorrect.setProducer(producerToValidate);
        entityToCorrect.setModel(modelToValidate);
        return airplaneTypeMapper.toResponseDto(entityToCorrect);

    }

    @Transactional
    public ResponseAirplaneTypeDto deactivateAirplaneType(Long id) {

        AirplaneTypeEntity entityToDeactivate = airplaneTypeRepository.findById(id).
                orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        if(entityToDeactivate.getStatus() == AirplaneTypeStatus.INACTIVE)
            return airplaneTypeMapper.toResponseDto(entityToDeactivate);

        if(!entityToDeactivate.getStatus().equals(AirplaneTypeStatus.ACTIVE))
            throw new AirplaneTypeInvalidStatusChangeException(entityToDeactivate.getStatus(), AirplaneTypeStatus.INACTIVE);

        entityToDeactivate.setStatus(AirplaneTypeStatus.INACTIVE);
        return airplaneTypeMapper.toResponseDto(entityToDeactivate);
    }

    @Transactional
    public ResponseAirplaneTypeDto activateAirplaneType(Long id) {

        AirplaneTypeEntity entityToActivate = airplaneTypeRepository.findById(id).
                orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        if(entityToActivate.getStatus() == AirplaneTypeStatus.RETIRED)
            throw new AirplaneTypeInvalidStatusChangeException(
                    AirplaneTypeStatus.RETIRED, AirplaneTypeStatus.ACTIVE
            );

        if(entityToActivate.getStatus() == AirplaneTypeStatus.ACTIVE)
            return airplaneTypeMapper.toResponseDto(entityToActivate);

        entityToActivate.setStatus(AirplaneTypeStatus.ACTIVE);
        return airplaneTypeMapper.toResponseDto(entityToActivate);
    }

    @Transactional
    public ResponseAirplaneTypeDto retireAirplaneType(Long id) {

        AirplaneTypeEntity entityToRetire = airplaneTypeRepository.findById(id).
                orElseThrow(()-> new AirplaneTypeDoesNotExistException(id));

        if(entityToRetire.getStatus().equals(AirplaneTypeStatus.RETIRED))
            return airplaneTypeMapper.toResponseDto(entityToRetire);

        if(!entityToRetire.getStatus().equals(AirplaneTypeStatus.INACTIVE))
            throw new AirplaneTypeInvalidStatusChangeException(entityToRetire.getStatus(), AirplaneTypeStatus.RETIRED);

        entityToRetire.setStatus(AirplaneTypeStatus.RETIRED);
        return airplaneTypeMapper.toResponseDto(airplaneTypeRepository.save(entityToRetire));
    }

}
