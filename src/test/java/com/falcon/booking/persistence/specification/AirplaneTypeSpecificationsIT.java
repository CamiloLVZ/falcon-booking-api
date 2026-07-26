package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.repository.BaseRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirplaneTypeSpecificationsIT extends BaseRepositoryTest {

    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    @BeforeEach
    void setUp() {
        airplaneTypeRepository.save(createAirplaneType("Airbus", "A320", AirplaneTypeStatus.ACTIVE));
        airplaneTypeRepository.save(createAirplaneType("Airbus", "A380", AirplaneTypeStatus.INACTIVE));
        airplaneTypeRepository.save(createAirplaneType("Boeing", "737", AirplaneTypeStatus.ACTIVE));
        airplaneTypeRepository.save(createAirplaneType("Boeing", "787", AirplaneTypeStatus.RETIRED));
    }

    private AirplaneTypeEntity createAirplaneType(String producer, String model, AirplaneTypeStatus status) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setProducer(producer);
        entity.setModel(model);
        entity.configureSeats(120, 0, "ABCDEF");
        entity.setStatus(status);
        return entity;
    }

    @DisplayName("Should filter by producer")
    @Test
    void shouldFilterByProducer() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasProducer("Airbus");

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(a -> assertThat(a.getProducer()).isEqualTo("Airbus"));
    }

    @DisplayName("Should return all when producer is null")
    @Test
    void shouldReturnAllWhenProducerIsNull() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasProducer(null);

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter by model")
    @Test
    void shouldFilterByModel() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasModel("737");

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("737");
    }

    @DisplayName("Should return all when model is null")
    @Test
    void shouldReturnAllWhenModelIsNull() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasModel(null);

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter by status")
    @Test
    void shouldFilterByStatus() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasStatus(AirplaneTypeStatus.ACTIVE);

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(a -> assertThat(a.getStatus()).isEqualTo(AirplaneTypeStatus.ACTIVE));
    }

    @DisplayName("Should return all when status is null")
    @Test
    void shouldReturnAllWhenStatusIsNull() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasStatus(null);

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should combine multiple filters")
    @Test
    void shouldCombineFilters() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasProducer("Airbus")
                .and(AirplaneTypeSpecifications.hasStatus(AirplaneTypeStatus.ACTIVE));

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("A320");
    }

    @DisplayName("Should return empty when no match")
    @Test
    void shouldReturnEmptyWhenNoMatch() {
        Specification<AirplaneTypeEntity> spec = AirplaneTypeSpecifications.hasProducer("Embraer");

        List<AirplaneTypeEntity> result = airplaneTypeRepository.findAll(spec);

        assertThat(result).isEmpty();
    }
}
