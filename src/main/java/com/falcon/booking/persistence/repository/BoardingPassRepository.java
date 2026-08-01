package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardingPassRepository extends JpaRepository<BoardingPassEntity,Long> {
    Optional<BoardingPassEntity> findByPassengerReservation(PassengerReservationEntity passengerReservation);
    @EntityGraph(attributePaths = {
            "passengerReservation.passenger",
            "passengerReservation.flight.airplaneType",
            "passengerReservation.flight.route.airportOrigin",
            "passengerReservation.flight.route.airportDestination"
    })
    Optional<BoardingPassEntity> findByQrToken(UUID qrToken);
}
