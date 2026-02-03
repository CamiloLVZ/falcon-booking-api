package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerReservationRepository extends JpaRepository<PassengerReservationEntity, Long> {
}
