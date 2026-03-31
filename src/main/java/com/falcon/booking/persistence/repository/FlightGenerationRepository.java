package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightGenerationRepository extends JpaRepository<FlightGenerationEntity, Long> {
}
