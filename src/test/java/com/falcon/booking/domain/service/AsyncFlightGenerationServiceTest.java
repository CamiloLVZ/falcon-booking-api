package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationNotFoundException;
import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AsyncFlightGenerationServiceTest {

    @Mock
    private FlightGenerationRepository flightGenerationRepository;

    @Mock
    private TransactionalFlightGenerationService transactionalFlightGenerationService;

    @InjectMocks
    private AsyncFlightGenerationService asyncFlightGenerationService;

    @DisplayName("Should successfully execute a global type flights generation")
    @Test
    void shouldExecuteGlobalFlightsGeneration(){
        FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
        generation.setId(1L);
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.of(generation));
        given(transactionalFlightGenerationService.generateAllFlightsForAllRoutes()).willReturn(1000);

        asyncFlightGenerationService.executeGeneration(1L);

        verify(flightGenerationRepository).save(generation);
    }

    @DisplayName("Should successfully execute a route type flights generation")
    @Test
    void shouldExecuteRouteFlightsGeneration(){
        FlightGenerationEntity generation = FlightGenerationEntity.startRouteGeneration(1L);
        generation.setId(1L);
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.of(generation));
        given(transactionalFlightGenerationService.generateAllFlightsForRoute(1L)).willReturn(200);

        asyncFlightGenerationService.executeGeneration(1L);

        verify(flightGenerationRepository).save(generation);
    }

    @DisplayName("Should successfully execute a daily type flights generation")
    @Test
    void shouldExecuteDailyFlightsGeneration(){
        FlightGenerationEntity generation = FlightGenerationEntity.startDailyGeneration(LocalDate.now());
        generation.setId(1L);
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.of(generation));
        given(transactionalFlightGenerationService.generateFlightsForAllRoutesAtHorizon()).willReturn(200);

        asyncFlightGenerationService.executeGeneration(1L);

        verify(flightGenerationRepository).save(generation);
    }

    @DisplayName("Should mark generation as failed when global generation throws")
    @Test
    void shouldMarkGenerationAsFailedWhenGlobalGenerationThrows(){
        FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
        generation.setId(1L);
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.of(generation));
        given(transactionalFlightGenerationService.generateAllFlightsForAllRoutes())
                .willThrow(new RuntimeException("duplicate key"));

        asyncFlightGenerationService.executeGeneration(1L);

        assertEquals(FlightGenerationStatus.FAILED, generation.getStatus());
        verify(flightGenerationRepository).save(generation);
    }

    @DisplayName("Should Throw FlightGenerationNotFoundException when generation not found")
    @Test
    void shouldThrowException_whenNotFound(){
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(FlightGenerationNotFoundException.class, () -> asyncFlightGenerationService.executeGeneration(1L));
    }
}
