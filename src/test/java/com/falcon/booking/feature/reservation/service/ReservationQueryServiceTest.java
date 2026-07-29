package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationQueryServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private FlightQueryService flightQueryService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;

    @InjectMocks
    private ReservationQueryService reservationQueryService;

    private CountryEntity createCountry(String isoCode) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName("Country " + isoCode);
        return country;
    }

    private FlightEntity createFlight(FlightStatus status, int economySeats, int firstClassSeats) {
        CountryEntity country = createCountry("CO");

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setName("El Dorado");
        origin.setCity("Bogota");
        origin.setCountry(country);
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MDE");
        destination.setName("JMC");
        destination.setCity("Medellin");
        destination.setCountry(country);
        destination.setTimezone("America/Bogota");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.configureSeats(economySeats, firstClassSeats, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setDurationMinutes(60);
        route.setStatus(RouteStatus.ACTIVE);

        FlightEntity flight = new FlightEntity();
        flight.setId(5L);
        flight.setRoute(route);
        flight.setAirplaneType(airplaneType);
        flight.setDepartureDateTime(OffsetDateTime.now().plusDays(1));
        flight.setStatus(status);
        return flight;
    }

    @DisplayName("Should return reservation entity by number")
    @Test
    void shouldReturnEntity_getReservationEntityByNumber() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.of(reservation));

        ReservationEntity result = reservationQueryService.getReservationEntityByNumber(" abc123 ");

        assertThat(result).isEqualTo(reservation);
        verify(reservationRepository).findByNumber("ABC123");
    }

    @DisplayName("Should throw exception when reservation does not exist")
    @Test
    void shouldThrowException_getReservationEntityByNumber() {
        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.empty());

        ReservationNotFoundException ex = assertThrows(ReservationNotFoundException.class,
                () -> reservationQueryService.getReservationEntityByNumber("abc123"));

        assertThat(ex.getMessage()).contains("ABC123");
    }

    @DisplayName("Should return active reservation entities by flight")
    @Test
    void shouldReturnEntities_getAllReservationEntitiesActiveByFlight() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("reservationDatetime").ascending());

        given(reservationRepository.findAllByFlightAndStatus(flight, ReservationStatus.RESERVED, pageable))
                .willReturn(new PageImpl<>(List.of(reservation), pageable, 1));

        Page<ReservationEntity> result = reservationQueryService.getAllReservationEntitiesActiveByFlight(flight, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("Should return my reservations without status filter")
    @Test
    void shouldReturnMyReservations_getMyReservations() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("reservationDatetime").descending());
        Page<ReservationEntity> reservationPage = new PageImpl<>(List.of(reservation), pageable, 1);
        ResponseReservationDto dto = new ResponseReservationDto("ABC123", "mail@test.com",
                Instant.now(), ReservationStatus.RESERVED, null, List.of());

        given(reservationRepository.findAllByUser(user, pageable)).willReturn(reservationPage);
        given(reservationMapper.toResponseDto(reservation)).willReturn(dto);

        Page<ResponseReservationDto> result = reservationQueryService.getMyReservations(user, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).number()).isEqualTo("ABC123");
        verify(reservationRepository).findAllByUser(user, pageable);
    }

    @DisplayName("Should return my reservations filtered by status")
    @Test
    void shouldReturnMyReservations_getMyReservations_withStatus() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("reservationDatetime").descending());
        Page<ReservationEntity> reservationPage = new PageImpl<>(List.of(reservation), pageable, 1);
        ResponseReservationDto dto = new ResponseReservationDto("ABC123", "mail@test.com",
                Instant.now(), ReservationStatus.RESERVED, null, List.of());

        given(reservationRepository.findAllByUserAndStatus(user, ReservationStatus.RESERVED, pageable))
                .willReturn(reservationPage);
        given(reservationMapper.toResponseDto(reservation)).willReturn(dto);

        Page<ResponseReservationDto> result = reservationQueryService.getMyReservations(user, ReservationStatus.RESERVED, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(ReservationStatus.RESERVED);
        verify(reservationRepository).findAllByUserAndStatus(user, ReservationStatus.RESERVED, pageable);
    }

    @DisplayName("Should return reservation by id")
    @Test
    void shouldReturnReservationById() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        ResponseReservationDto dto = new ResponseReservationDto("ABC123", "mail@test.com",
                Instant.now(), ReservationStatus.RESERVED, null, List.of());
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationMapper.toResponseDto(reservation)).willReturn(dto);

        ResponseReservationDto result = reservationQueryService.getReservationById(1L);

        assertThat(result).isEqualTo(dto);
        verify(reservationRepository).findById(1L);
    }

    @DisplayName("Should throw exception when reservation by id does not exist")
    @Test
    void shouldThrowException_getReservationById() {
        given(reservationRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationQueryService.getReservationById(99L));
    }

    @DisplayName("Should return passenger reservations summary (last 3)")
    @Test
    void shouldReturnPassengerReservationsSummary() {
        PassengerEntity passenger = new PassengerEntity();
        passenger.setId(1L);
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        PassengerReservationEntity pr = new PassengerReservationEntity(passenger, reservation, 1, SeatClass.ECONOMY);
        Pageable topThree = PageRequest.of(0, 3);
        ResponsePassengerReservationDto dto = new ResponsePassengerReservationDto(1L, null, 1, null, SeatClass.ECONOMY, null);

        given(passengerService.getPassengerEntityById(1L)).willReturn(passenger);
        given(passengerReservationRepository.findTop3ByPassengerOrderByReservationDatetimeDesc(passenger, topThree))
                .willReturn(List.of(pr));
        given(passengerReservationMapper.toResponseDto(List.of(pr))).willReturn(List.of(dto));

        List<ResponsePassengerReservationDto> result = reservationQueryService.getPassengerReservationsSummary(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().seatNumber()).isEqualTo(1);
        verify(passengerService).getPassengerEntityById(1L);
        verify(passengerReservationRepository).findTop3ByPassengerOrderByReservationDatetimeDesc(passenger, topThree);
        verify(passengerReservationMapper).toResponseDto(List.of(pr));
    }
}
