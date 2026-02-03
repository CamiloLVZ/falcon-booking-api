package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerReservationRepository extends JpaRepository<PassengerReservationEntity, Long> {

    List<PassengerReservationEntity> findAllBySeatNumberAndFlight(Integer seatNumber, FlightEntity flight);
    List<PassengerReservationEntity> findAllByPassenger(PassengerEntity passenger);
}
