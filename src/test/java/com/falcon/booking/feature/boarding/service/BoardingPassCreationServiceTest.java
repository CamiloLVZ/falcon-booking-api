package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardingPassCreationServiceTest {

    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private BoardingPassRepository boardingPassRepository;

    private BoardingPassCreationService boardingPassCreationService;

    @BeforeEach
    void setUp() {
        boardingPassCreationService = new BoardingPassCreationService(passengerReservationRepository, boardingPassRepository);
    }

    private PassengerReservationEntity buildPassengerReservation() {
        FlightEntity flight = new FlightEntity();
        PassengerEntity passenger = new PassengerEntity();
        ReservationEntity reservation = new ReservationEntity("RES123", flight, "test@test.com", Instant.now());
        return new PassengerReservationEntity(passenger, reservation, 5, SeatClass.ECONOMY);
    }

    @DisplayName("Should create boarding pass synchronously if not present")
    @Test
    void shouldCreateBoardingPass_whenNotPresent() {
        PassengerReservationEntity pr = buildPassengerReservation();
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());
        when(boardingPassRepository.save(any(BoardingPassEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        BoardingPassEntity created = boardingPassCreationService.createBoardingPass(10L);

        assertNotNull(created);
        verify(boardingPassRepository).save(any(BoardingPassEntity.class));
    }

    @DisplayName("Should return existing boarding pass when calling createBoardingPass")
    @Test
    void shouldReturnExistingBoardingPass_whenCreateBoardingPassCalled() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity existingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(existingPass));

        BoardingPassEntity result = boardingPassCreationService.createBoardingPass(10L);

        assertEquals(existingPass, result);
        verify(boardingPassRepository, never()).save(any());
    }

    @DisplayName("Should throw PassengerReservationNotFoundException when reservation does not exist")
    @Test
    void shouldThrowException_whenPassengerReservationNotFound() {
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                PassengerReservationNotFoundException.class,
                () -> boardingPassCreationService.createBoardingPass(10L)
        );
        verify(boardingPassRepository, never()).save(any());
    }
}
