package com.falcon.booking.feature.flightGeneration.service;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlightBatchPersistenceServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FlightBatchPersistenceService flightBatchPersistenceService;

    @DisplayName("Should save batch and flush entity manager")
    @Test
    void shouldSaveBatchAndFlush() {
        ReflectionTestUtils.setField(flightBatchPersistenceService, "entityManager", entityManager);
        List<FlightEntity> batch = List.of(new FlightEntity());

        flightBatchPersistenceService.saveBatch(batch);

        verify(flightRepository).saveAll(batch);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }
}
