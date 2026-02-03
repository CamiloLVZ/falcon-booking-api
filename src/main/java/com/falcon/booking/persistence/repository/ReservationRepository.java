package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    boolean existsByNumber(String number);
    Optional<ReservationEntity> findByNumber(String number);
    List<ReservationEntity> findAllByFlightAndStatus(FlightEntity flight, ReservationStatus status);
}
