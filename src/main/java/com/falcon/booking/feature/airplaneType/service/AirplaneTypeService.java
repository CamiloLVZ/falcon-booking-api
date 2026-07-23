package com.falcon.booking.feature.airplaneType.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.airplaneType.dto.*;
import com.falcon.booking.feature.airplaneType.exception.AirplaneNotFoundException;
import com.falcon.booking.feature.airplaneType.exception.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.feature.airplaneType.mapper.AirplaneTypeMapper;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.specification.AirplaneTypeSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public AirplaneTypeEntity getAirplaneTypeEntity(Long id) {
        return airplaneTypeRepository.findById(id)
                .orElseThrow(() -> new AirplaneNotFoundException(id));
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
        if (exists) throw new AirplaneTypeAlreadyExistsException(producer, model);

        AirplaneTypeEntity entityToSave = airplaneTypeMapper.toEntity(createAirplaneTypeDto);
        entityToSave.configureSeats(
                createAirplaneTypeDto.economySeats(),
                createAirplaneTypeDto.firstClassSeats(),
                createAirplaneTypeDto.seatColumns()
        );
        entityToSave.activate();

        AirplaneTypeEntity entityCreated = airplaneTypeRepository.save(entityToSave);
        logger.info("Airplane Type created: {}", entityCreated.getFullName());
        return airplaneTypeMapper.toResponseDto(entityCreated);
    }

    public List<SeatDefinition> getSeats(AirplaneTypeEntity airplaneTypeEntity) {

        List<SeatDefinition> seats = new ArrayList<>();

        for (int seat = 1; seat <= airplaneTypeEntity.getTotalSeats(); seat++) {
            seats.add(new SeatDefinition(seat, airplaneTypeEntity.getSeatLabel(seat), airplaneTypeEntity.getSeatClass(seat)));
        }
        return seats;
    }

    @Transactional
    public ResponseAirplaneTypeDto configureSeats(Long id, ConfigureSeatsDto configureSeatsDto) {

        AirplaneTypeEntity entity = getAirplaneTypeEntity(id);
        entity.configureSeats(
                configureSeatsDto.economySeats(),
                configureSeatsDto.firstClassSeats(),
                configureSeatsDto.seatColumns()
        );

        logger.info("Airplane Type {} seat configuration updated", entity.getFullName());
        return airplaneTypeMapper.toResponseDto(entity);
    }

    @Transactional
    public ResponseAirplaneTypeDto correctAirplaneType(Long id, CorrectAirplaneTypeDto correctAirplaneTypeDto) {

        AirplaneTypeEntity entityToCorrect = getAirplaneTypeEntity(id);
        String newProducer = StringNormalizer.normalize(correctAirplaneTypeDto.producer());
        String newModel = StringNormalizer.normalize(correctAirplaneTypeDto.model());

        String producerToValidate = newProducer != null ? newProducer : entityToCorrect.getProducer();
        String modelToValidate = newModel != null ? newModel : entityToCorrect.getModel();

        boolean isChanging = !entityToCorrect.getModel().equals(modelToValidate)
                || !entityToCorrect.getProducer().equals(producerToValidate);

        if (!isChanging) return airplaneTypeMapper.toResponseDto(entityToCorrect);

        if (airplaneTypeRepository.existsByProducerAndModel(producerToValidate, modelToValidate)) {
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
