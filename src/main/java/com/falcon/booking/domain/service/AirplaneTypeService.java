package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneNotFoundException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.specification.AirplaneTypeSpecifications;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AirplaneTypeService {

    private static final Logger logger = LoggerFactory.getLogger(AirplaneTypeService.class);
    private final AirplaneTypeRepository airplaneTypeRepository;
    private final AirplaneTypeMapper airplaneTypeMapper;

    @Autowired
    public AirplaneTypeService(AirplaneTypeRepository airplaneTypeRepository, AirplaneTypeMapper airplaneTypeMapper) {
        this.airplaneTypeRepository = airplaneTypeRepository;
        this.airplaneTypeMapper = airplaneTypeMapper;
    }

    public AirplaneTypeEntity getAirplaneTypeEntity(Long id){
        return airplaneTypeRepository.findById(id).
                orElseThrow(() -> new AirplaneNotFoundException(id));
    }

    public ResponseAirplaneTypeDto getAirplaneTypeById(Long id) {
        AirplaneTypeEntity airplaneTypeEntity = getAirplaneTypeEntity(id);
        return airplaneTypeMapper.toResponseDto(airplaneTypeEntity);
    }

    public List<ResponseAirplaneTypeDto> getAirplaneTypes(String producer, String model, AirplaneTypeStatus status) {

        producer = StringNormalizer.normalize(producer);
        model = StringNormalizer.normalize(model);

        Specification<AirplaneTypeEntity> specification = Specification.allOf();
        specification = specification.and(AirplaneTypeSpecifications.hasModel(model));
        specification = specification.and(AirplaneTypeSpecifications.hasProducer(producer));
        specification = specification.and(AirplaneTypeSpecifications.hasStatus(status));

        List<AirplaneTypeEntity> entities = airplaneTypeRepository.findAll(specification);

        return airplaneTypeMapper.toResponseDto(entities);
    }

    @Transactional
    public ResponseAirplaneTypeDto addAirplaneType(CreateAirplaneTypeDto createAirplaneTypeDto) {
        String producer = createAirplaneTypeDto.producer();
        String model = createAirplaneTypeDto.model();

        boolean exists = airplaneTypeRepository.existsByProducerAndModel(producer, model);
        if(exists) throw new AirplaneTypeAlreadyExistsException(producer, model);

        AirplaneTypeEntity entityToSave = airplaneTypeMapper.toEntity(createAirplaneTypeDto);
        entityToSave.activate();
        AirplaneTypeEntity entityCreated = airplaneTypeRepository.save(entityToSave);
        logger.info("Airplane Type created: {}", entityCreated.getFullName());
        return airplaneTypeMapper.toResponseDto(entityCreated);
    }

    @Transactional
    public ResponseAirplaneTypeDto updateAirplaneType(Long id, UpdateAirplaneTypeDto updateAirplaneTypeDto) {

        AirplaneTypeEntity entityToUpdate = getAirplaneTypeEntity(id);

        if(updateAirplaneTypeDto.economySeats()!=null) entityToUpdate.setEconomySeats(updateAirplaneTypeDto.economySeats());
        if(updateAirplaneTypeDto.firstClassSeats()!=null) entityToUpdate.setFirstClassSeats(updateAirplaneTypeDto.firstClassSeats());
        if(updateAirplaneTypeDto.status()!=null) entityToUpdate.setStatus(updateAirplaneTypeDto.status());

        logger.info("Airplane Type {} updated", entityToUpdate.getFullName());
        return airplaneTypeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public ResponseAirplaneTypeDto correctAirplaneType(Long id, CorrectAirplaneTypeDto correctAirplaneTypeDto) {

        AirplaneTypeEntity entityToCorrect = getAirplaneTypeEntity(id);

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
        logger.info("Airplane Type {} corrected", entityToCorrect.getFullName());
        return airplaneTypeMapper.toResponseDto(entityToCorrect);
    }

    @Transactional
    public ResponseAirplaneTypeDto deactivateAirplaneType(Long id) {

        AirplaneTypeEntity entityToDeactivate = getAirplaneTypeEntity(id);
        entityToDeactivate.deactivate();

        logger.info("Airplane Type {} changed status to INACTIVE", entityToDeactivate.getFullName());
        return airplaneTypeMapper.toResponseDto(entityToDeactivate);
    }

    @Transactional
    public ResponseAirplaneTypeDto activateAirplaneType(Long id) {

        AirplaneTypeEntity entityToActivate = getAirplaneTypeEntity(id);
        entityToActivate.activate();

        logger.info("Airplane Type {} changed status to ACTIVE", entityToActivate.getFullName());
        return airplaneTypeMapper.toResponseDto(entityToActivate);
    }

    @Transactional
    public ResponseAirplaneTypeDto retireAirplaneType(Long id) {

        AirplaneTypeEntity entityToRetire = getAirplaneTypeEntity(id);
        entityToRetire.retire();

        logger.info("Airplane Type {} changed status to RETIRED", entityToRetire.getFullName());
        return airplaneTypeMapper.toResponseDto(entityToRetire);
    }

}
