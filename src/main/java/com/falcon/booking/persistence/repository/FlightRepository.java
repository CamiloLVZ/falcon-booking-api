package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long>, JpaSpecificationExecutor<FlightEntity> {
    Boolean existsByRouteAndDepartureDateTime(RouteEntity route, OffsetDateTime departureDateTime);
    List<FlightEntity> findAllByRouteAndDepartureDateTimeBetween(RouteEntity route, OffsetDateTime departureDateTime, OffsetDateTime departureDateTime2);
    List<FlightEntity> findAllByStatusNotAndStatusNot(FlightStatus status, FlightStatus status2);

    @Query("SELECT f.departureDateTime FROM FlightEntity f " +
            "WHERE f.route = :route " +
            "AND f.departureDateTime IN :departureTimes")
    List<OffsetDateTime> findExistingDepartureTimes(
            @Param("route") RouteEntity route,
            @Param("departureTimes") List<OffsetDateTime> departureTimes
    );

    @Query("""
            SELECT f.departureDateTime FROM FlightEntity f
            WHERE f.route.id = :routeId AND f.departureDateTime >= :start AND f.departureDateTime < :end""")
    List<OffsetDateTime> findExistingDepartureTimesInRange(Long routeId, OffsetDateTime start, OffsetDateTime end);

    @Query("""
    SELECT f FROM FlightEntity f
    WHERE f.route.airportOrigin.iataCode = :origin
      AND f.route.airportDestination.iataCode = :destination
      AND f.departureDateTime >= :start
      AND f.departureDateTime < :end
      AND f.status = :status
    ORDER BY f.departureDateTime ASC
""")
    @EntityGraph(attributePaths = {"airplaneType", "route", "route.airportOrigin", "route.airportDestination"})
    List<FlightEntity> findFlightsByAirportsAndDate(String origin, String destination, OffsetDateTime start, OffsetDateTime end, FlightStatus status);


}
