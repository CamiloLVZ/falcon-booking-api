package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Route.RouteInvalidStatusChangeException;
import com.falcon.booking.domain.exception.Route.RouteNotActivableException;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RouteEntityTest {

    private RouteEntity createRoute(String flightNumber) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        return route;
    }

    private AirplaneTypeEntity createActiveAirplaneType() {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        return airplaneType;
    }

    private AirportEntity createAirport(Long id, String iataCode) {
        AirportEntity airport = new AirportEntity();
        airport.setId(id);
        airport.setIataCode(iataCode);
        return airport;
    }

    private RouteEntity createActivableRoute() {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");
        route.setAirportOrigin(createAirport(1L, "BOG"));
        route.setAirportDestination(createAirport(2L, "MDE"));
        route.setLengthMinutes(60);
        route.setDefaultAirplaneType(createActiveAirplaneType());

        List<RouteDayEntity> routeDays = new ArrayList<>();
        routeDays.add(new RouteDayEntity(route, DayOfWeek.MONDAY));
        route.setRouteDays(routeDays);

        List<RouteScheduleEntity> routeSchedules = new ArrayList<>();
        routeSchedules.add(new RouteScheduleEntity(route, LocalTime.of(8, 0)));
        route.setRouteSchedules(routeSchedules);

        return route;
    }

    @DisplayName("Should activate route when all requirements are met")
    @Test
    void shouldActivateRoute_activate() {
        RouteEntity route = createActivableRoute();

        route.activate();

        assertThat(route.isActive()).isTrue();
    }

    @DisplayName("Should throw exception when route can not be activated")
    @Test
    void shouldThrowException_activate() {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");

        RouteNotActivableException exception = assertThrows(RouteNotActivableException.class, route::activate);

        assertThat(exception.getMessage()).isNotEmpty();
    }

    @DisplayName("Should deactivate active route")
    @Test
    void shouldDeactivateRoute_deactivate() {
        RouteEntity route = createRoute("AV1234");
        route.setStatus(RouteStatus.ACTIVE);

        route.deactivate();

        assertThat(route.isInactive()).isTrue();
    }

    @DisplayName("Should throw exception when deactivating draft route")
    @Test
    void shouldThrowException_deactivate() {
        RouteEntity route = createRoute("AV1234");
        route.setStatus(RouteStatus.DRAFT);

        RouteInvalidStatusChangeException exception =
                assertThrows(RouteInvalidStatusChangeException.class, route::deactivate);

        assertThat(exception.getMessage()).contains("DRAFT to INACTIVE");
    }

    @DisplayName("Should mark route as draft when status is null")
    @Test
    void shouldMarkAsDraft_markAsDraft() {
        RouteEntity route = createRoute("AV1234");

        route.markAsDraft();

        assertThat(route.isDraft()).isTrue();
    }

    @DisplayName("Should update weekdays with unique values")
    @Test
    void shouldUpdateWeekDays_updateWeekDays() {
        RouteEntity route = createActivableRoute();

        route.updateWeekDays(List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        Set<DayOfWeek> operatingDays = route.getOperatingDays();
        assertThat(operatingDays).hasSize(2);
        assertThat(operatingDays).contains(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @DisplayName("Should update schedules with unique values")
    @Test
    void shouldUpdateSchedules_updateSchedules() {
        RouteEntity route = createActivableRoute();

        route.updateSchedules(List.of(LocalTime.of(8, 0), LocalTime.of(8, 0), LocalTime.of(10, 0)));

        Set<LocalTime> schedules = route.getOperatingSchedules();
        assertThat(schedules).hasSize(2);
        assertThat(schedules).contains(LocalTime.of(8, 0), LocalTime.of(10, 0));
    }

    @DisplayName("Equals methods for entities with same flight number should return true")
    @Test
    void shouldReturnTrue_sameRouteInEquals() {
        RouteEntity route1 = createRoute("AV1234");
        RouteEntity route2 = createRoute("AV1234");

        boolean result = route1.equals(route2);

        assertThat(result).isTrue();
    }

    @DisplayName("Hash code for entities with different flight number should not be equal")
    @Test
    void shouldNotBeEqual_differentRouteHashCode() {
        RouteEntity route1 = createRoute("AV1234");
        RouteEntity route2 = createRoute("AV7777");

        int hashCode1 = route1.hashCode();
        int hashCode2 = route2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }
}
