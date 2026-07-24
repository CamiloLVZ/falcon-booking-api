package com.falcon.booking.feature.airplaneType.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.dto.ConfigureSeatsDto;
import com.falcon.booking.feature.airplaneType.dto.CorrectAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.CreateAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.exception.AirplaneNotFoundException;
import com.falcon.booking.feature.airplaneType.exception.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.feature.airplaneType.mapper.AirplaneTypeMapper;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

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

    /**
     * Builds a test entity using the domain methods (no public setters for seat fields).
     * Uses ReflectionTestUtils only to set the auto-generated id.
     */
    private AirplaneTypeEntity createEntity(Long id) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setProducer("Airbus");
        entity.setModel("A320");
        entity.configureSeats(108, 12, "ABCDEF");
        entity.setStatus(AirplaneTypeStatus.INACTIVE);
        if (id != null) {
            ReflectionTestUtils.setField(entity, "id", id);
        }
        return entity;
    }

    private ResponseAirplaneTypeDto createDto() {
        return new ResponseAirplaneTypeDto(
                1L,
                "Airbus",
                "A320",
                108,
                12,
                "ABCDEF",
                AirplaneTypeStatus.ACTIVE
        );
    }

    // ─── getAirplaneTypeEntity ────────────────────────────────────────────────

    @DisplayName("Should return AirplaneTypeEntity when it exists")
    @Test
    void shouldReturnEntity_getAirplaneTypeEntity() {
        AirplaneTypeEntity entity = createEntity(1L);
        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));

        AirplaneTypeEntity result = airplaneTypeService.getAirplaneTypeEntity(1L);

        assertThat(result).isSameAs(entity);
    }

    @DisplayName("Should throw AirplaneNotFoundException when entity not found")
    @Test
    void shouldThrowException_getAirplaneTypeEntity() {
        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(AirplaneNotFoundException.class,
                () -> airplaneTypeService.getAirplaneTypeEntity(1L));
    }

    // ─── getAirplaneTypeById ──────────────────────────────────────────────────

    @DisplayName("Should return response dto when getting by id")
    @Test
    void shouldReturnDto_getAirplaneTypeById() {
        AirplaneTypeEntity entity = createEntity(1L);
        ResponseAirplaneTypeDto dto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(dto);

        ResponseAirplaneTypeDto result = airplaneTypeService.getAirplaneTypeById(1L);

        assertThat(result).isEqualTo(dto);
    }

    // ─── getAirplaneTypes ─────────────────────────────────────────────────────

    @DisplayName("Should return list of airplane types matching filters")
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

    // ─── addAirplaneType ──────────────────────────────────────────────────────

    @DisplayName("Should create airplane type and activate it when it does not exist")
    @Test
    void shouldCreateAirplaneType_addAirplaneType() {
        CreateAirplaneTypeDto createDto =
                new CreateAirplaneTypeDto("Airbus", "A320", 108, 12, "ABCDEF");

        AirplaneTypeEntity entity = createEntity(null);
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.existsByProducerAndModel(any(), any())).willReturn(false);
        given(airplaneTypeMapper.toEntity(createDto)).willReturn(entity);
        given(airplaneTypeRepository.save(entity)).willReturn(entity);
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        ResponseAirplaneTypeDto result = airplaneTypeService.addAirplaneType(createDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getEconomySeats()).isEqualTo(108);
        assertThat(entity.getFirstClassSeats()).isEqualTo(12);
        assertThat(entity.getSeatColumns()).isEqualTo("ABCDEF");
    }

    @DisplayName("Should throw AirplaneTypeAlreadyExistsException when airplane type already exists")
    @Test
    void shouldThrowException_addAirplaneType() {
        CreateAirplaneTypeDto createDto =
                new CreateAirplaneTypeDto("Airbus", "A320", 108, 12, "ABCDEF");

        given(airplaneTypeRepository.existsByProducerAndModel(any(), any())).willReturn(true);

        assertThrows(AirplaneTypeAlreadyExistsException.class,
                () -> airplaneTypeService.addAirplaneType(createDto));
    }

    // ─── configureSeats ───────────────────────────────────────────────────────

    @DisplayName("Should update seat configuration when configureSeats is called")
    @Test
    void shouldConfigureSeats_configureSeats() {
        AirplaneTypeEntity entity = createEntity(1L);
        ConfigureSeatsDto configureSeatsDto = new ConfigureSeatsDto(198, 24, "ABCDEF");
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        ResponseAirplaneTypeDto result = airplaneTypeService.configureSeats(1L, configureSeatsDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getEconomySeats()).isEqualTo(198);
        assertThat(entity.getFirstClassSeats()).isEqualTo(24);
        assertThat(entity.getSeatColumns()).isEqualTo("ABCDEF");
    }

    @DisplayName("Should throw AirplaneNotFoundException when configureSeats id does not exist")
    @Test
    void shouldThrowException_configureSeats_notFound() {
        given(airplaneTypeRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(AirplaneNotFoundException.class,
                () -> airplaneTypeService.configureSeats(99L, new ConfigureSeatsDto(100, 10, "ABC")));
    }

    // ─── correctAirplaneType ──────────────────────────────────────────────────

    @DisplayName("Should not change airplane type when correcting with same data")
    @Test
    void shouldNotChange_correctAirplaneType() {
        AirplaneTypeEntity entity = createEntity(1L);
        CorrectAirplaneTypeDto correctDto = new CorrectAirplaneTypeDto("Airbus", "A320");
        ResponseAirplaneTypeDto responseDto = createDto();

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeMapper.toResponseDto(entity)).willReturn(responseDto);

        ResponseAirplaneTypeDto result = airplaneTypeService.correctAirplaneType(1L, correctDto);

        assertThat(result).isEqualTo(responseDto);
        verify(airplaneTypeRepository, never()).save(any());
        verify(airplaneTypeRepository, never()).existsByProducerAndModel("Airbus", "A320");
    }

    @DisplayName("Should throw AirplaneTypeAlreadyExistsException when correcting to existing identity")
    @Test
    void shouldThrowException_correctAirplaneType() {
        AirplaneTypeEntity entity = createEntity(1L);
        CorrectAirplaneTypeDto correctDto = new CorrectAirplaneTypeDto("Boeing", "737");

        given(airplaneTypeRepository.findById(1L)).willReturn(Optional.of(entity));
        given(airplaneTypeRepository.existsByProducerAndModel(any(), any())).willReturn(true);

        assertThrows(AirplaneTypeAlreadyExistsException.class,
                () -> airplaneTypeService.correctAirplaneType(1L, correctDto));
    }

    // ─── Status changes ───────────────────────────────────────────────────────

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
