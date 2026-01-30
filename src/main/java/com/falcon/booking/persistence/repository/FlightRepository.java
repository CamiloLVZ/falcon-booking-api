package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long>, JpaSpecificationExecutor<FlightEntity> {
    Boolean existsByRouteAndDepartureDateTime(RouteEntity route, OffsetDateTime departureDateTime);
    List<FlightEntity> findAllByRouteAndDepartureDateTimeBetween(RouteEntity route, OffsetDateTime departureDateTime, OffsetDateTime departureDateTime2);

    @Query("SELECT f FROM FlightEntity f WHERE f.status= :status AND f.departureDateTime <= :latestDeparture")
    List<FlightEntity> findAllFlightsToStartBoarding(@Param("status") FlightStatus status, @Param("latestDeparture") OffsetDateTime latestDeparture);

    @Query("SELECT f FROM FlightEntity f WHERE f.status = :status AND f.departureDateTime <= :now")
    List<FlightEntity> findAllFlightsToComplete(@Param("status")FlightStatus status, @Param("now") OffsetDateTime now);

    @Query("SELECT f.departureDateTime FROM FlightEntity f " +
            "WHERE f.route = :route " +
            "AND f.departureDateTime IN :departureTimes")
    List<OffsetDateTime> findExistingDepartureTimes(
            @Param("route") RouteEntity route,
            @Param("departureTimes") List<OffsetDateTime> departureTimes
    );

}
