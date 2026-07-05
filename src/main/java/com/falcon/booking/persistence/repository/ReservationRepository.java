package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    boolean existsByNumber(String number);
    Optional<ReservationEntity> findByNumber(String number);
    Page<ReservationEntity> findAllByFlightAndStatus(FlightEntity flight, ReservationStatus status, Pageable pageable);
}
