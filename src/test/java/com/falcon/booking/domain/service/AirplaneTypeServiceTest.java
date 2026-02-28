package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneNotFoundException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AirplaneTypeServiceTest {

    @Mock
    private AirplaneTypeRepository airplaneTypeRepository;

    @Mock
    private AirplaneTypeMapper airplaneTypeMapper;

    @InjectMocks
    private AirplaneTypeService airplaneTypeService;

    private AirplaneTypeEntity createEntity(Long id) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setId(id);
        entity.setProducer("Airbus");
        entity.setModel("A320");
        entity.setEconomySeats(100);
        entity.setFirstClassSeats(10);
        entity.setStatus(AirplaneTypeStatus.INACTIVE);
        return entity;
    }

    private ResponseAirplaneTypeDto createDto() {
        return new ResponseAirplaneTypeDto(
                1L,
                "Airbus",
                "A320",
                100,
                10,
                AirplaneTypeStatus.ACTIVE
        );
    }

    @DisplayName("Should return AirplaneTypeEntity when exists")
    @Test
    void shouldReturnEntity_getAirplaneTypeEntity() {

        AirplaneTypeEntity entity = createEntity(1L);
        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));

        AirplaneTypeEntity result = airplaneTypeService.getAirplaneTypeEntity(1L);

        assertThat(result).isSameAs(entity);
    }

    @DisplayName("Should throw exception when AirplaneTypeEntity not found")
    @Test
    void shouldThrowException_getAirplaneTypeEntity() {

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(AirplaneNotFoundException.class,
                () -> airplaneTypeService.getAirplaneTypeEntity(1L));
    }

    @DisplayName("Should return dto when getting by id")
    @Test
    void shouldReturnDto_getAirplaneTypeById() {

        AirplaneTypeEntity entity = createEntity(1L);
        ResponseAirplaneTypeDto dto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(dto);

        ResponseAirplaneTypeDto result = airplaneTypeService.getAirplaneTypeById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should return list of airplane types with filters")
    @Test
    void shouldReturnList_getAirplaneTypes() {

        AirplaneTypeEntity entity = createEntity(1L);
        ResponseAirplaneTypeDto dto = createDto();

        given(airplaneTypeRepository.findAll(any(Specification.class)))
                .willReturn(List.of(entity));
        given(airplaneTypeMapper.toResponseDto(List.of(entity)))
                .willReturn(List.of(dto));

        List<ResponseAirplaneTypeDto> result =
                airplaneTypeService.getAirplaneTypes("Airbus", "A320", AirplaneTypeStatus.INACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @DisplayName("Should create airplane type when it does not exist")
    @Test
    void shouldCreateAirplaneType_addAirplaneType() {

        CreateAirplaneTypeDto createDto =
                new CreateAirplaneTypeDto("Airbus", "A320", 100, 10);

        AirplaneTypeEntity entity = createEntity(null);
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.existsByProducerAndModel(any(String.class),any(String.class)))
                .willReturn(false);
        given(airplaneTypeMapper.toEntity(createDto))
                .willReturn(entity);
        given(airplaneTypeRepository.save(entity))
                .willReturn(entity);
        given(airplaneTypeMapper.toResponseDto(entity))
                .willReturn(responseDto);

        ResponseAirplaneTypeDto result =
                airplaneTypeService.addAirplaneType(createDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.isActive()).isTrue();
    }

    @DisplayName("Should throw exception when airplane type already exists")
    @Test
    void shouldThrowException_addAirplaneType() {

        CreateAirplaneTypeDto createDto =
                new CreateAirplaneTypeDto("Airbus", "A320", 100, 10);

        given(airplaneTypeRepository.existsByProducerAndModel(any(String.class), any(String.class)))
                .willReturn(true);

        assertThrows(AirplaneTypeAlreadyExistsException.class,
                () -> airplaneTypeService.addAirplaneType(createDto));
    }

    @DisplayName("Should update airplane type fields")
    @Test
    void shouldUpdateAirplaneType_updateAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        UpdateAirplaneTypeDto updateDto =
                new UpdateAirplaneTypeDto(200, null);
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        ResponseAirplaneTypeDto result =
                airplaneTypeService.updateAirplaneType(1L, updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getEconomySeats()).isEqualTo(200);
    }

    @DisplayName("Should not change airplane type when correcting with same data")
    @Test
    void shouldNotChange_correctAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        CorrectAirplaneTypeDto correctDto =
                new CorrectAirplaneTypeDto("Airbus", "A320");
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        ResponseAirplaneTypeDto result =
                airplaneTypeService.correctAirplaneType(1L, correctDto);

        assertThat(result).isEqualTo(responseDto);
        verify(airplaneTypeRepository,never()).save(any());
        verify(airplaneTypeRepository,never()).existsByProducerAndModel("Airbus", "A320");
    }

    @DisplayName("Should throw exception when correcting to existing airplane type")
    @Test
    void shouldThrowException_correctAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        CorrectAirplaneTypeDto correctDto =
                new CorrectAirplaneTypeDto("Boeing", "737");

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeRepository.existsByProducerAndModel(any(String.class), any(String.class)))
                .willReturn(true);

        assertThrows(AirplaneTypeAlreadyExistsException.class,
                () -> airplaneTypeService.correctAirplaneType(1L, correctDto));
    }

    @DisplayName("Should deactivate airplane type")
    @Test
    void shouldDeactivateAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        entity.activate();
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        airplaneTypeService.deactivateAirplaneType(1L);

        assertThat(entity.isInactive()).isTrue();
    }

    @DisplayName("Should activate airplane type")
    @Test
    void shouldActivateAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        airplaneTypeService.activateAirplaneType(1L);

        assertThat(entity.isActive()).isTrue();
    }

    @DisplayName("Should retire airplane type")
    @Test
    void shouldRetireAirplaneType() {

        AirplaneTypeEntity entity = createEntity(1L);
        entity.deactivate();
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        airplaneTypeService.retireAirplaneType(1L);

        assertThat(entity.isRetired()).isTrue();
    }
}
