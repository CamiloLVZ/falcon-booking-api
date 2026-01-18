package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long> {
    Boolean existsByRouteAndDepartureDateTime(RouteEntity route, OffsetDateTime departureDateTime);
}
