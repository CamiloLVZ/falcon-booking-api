package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long>, JpaSpecificationExecutor<FlightEntity> {
    Boolean existsByRouteAndDepartureDateTime(RouteEntity route, OffsetDateTime departureDateTime);
    List<FlightEntity> findAllByRouteAndDepartureDateTimeBetween(RouteEntity route, OffsetDateTime departureDateTime, OffsetDateTime departureDateTime2);
}
