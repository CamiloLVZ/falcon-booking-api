package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class AirplaneTypeRepositoryTest {

    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private AirplaneTypeEntity createAirplaneType(String producer, String model) {
        AirplaneTypeEntity airplaneTypeEntity = new AirplaneTypeEntity();
        airplaneTypeEntity.setProducer(producer);
        airplaneTypeEntity.setModel(model);
        airplaneTypeEntity.setEconomySeats(100);
        airplaneTypeEntity.setFirstClassSeats(0);
        airplaneTypeEntity.setStatus(AirplaneTypeStatus.INACTIVE);
        return airplaneTypeEntity;
    }

    @DisplayName("Should return true when AirplaneType with producer and model exists")
    @Test
    void shouldReturnTrue_existsByProducerAndModel() {
        AirplaneTypeEntity airplaneTypeEntity = createAirplaneType("Airbus", "320");
        airplaneTypeRepository.save(airplaneTypeEntity);

        boolean exists = airplaneTypeRepository.existsByProducerAndModel("Airbus", "320");

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return true when AirplaneType with producer and model does not exists")
    @Test
    void shouldReturnFalse_existsByProducerAndModel() {
        AirplaneTypeEntity airplaneTypeEntity = createAirplaneType("Boeing", "787");
        airplaneTypeRepository.save(airplaneTypeEntity);

        boolean exists = airplaneTypeRepository.existsByProducerAndModel("Boeing", "737");

        assertThat(exists).isFalse();
    }

}
