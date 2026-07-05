package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerReservationRepository extends JpaRepository<PassengerReservationEntity, Long> {

    List<PassengerReservationEntity> findAllBySeatNumberAndFlight(Integer seatNumber, FlightEntity flight);

    @Query(value = "SELECT pr FROM PassengerReservationEntity pr " +
            "JOIN FETCH pr.reservation r " +
            "WHERE pr.passenger = :passenger " +
            "ORDER BY r.reservationDatetime ASC",
            countQuery = "SELECT COUNT(pr) FROM PassengerReservationEntity pr WHERE pr.passenger = :passenger")
    Page<PassengerReservationEntity> findAllByPassenger(@Param("passenger") PassengerEntity passenger, Pageable pageable);
}
