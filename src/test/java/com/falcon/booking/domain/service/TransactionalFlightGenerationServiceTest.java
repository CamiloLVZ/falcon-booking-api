package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationPartialFailureException;
import com.falcon.booking.domain.exception.Route.RouteNotFoundException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteDayEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.entity.RouteScheduleEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class TransactionalFlightGenerationServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private FlightBatchPersistenceService flightBatchPersistenceService;

    @Mock
    private Executor flightGenerationExecutor;

    private TransactionalFlightGenerationService createService() {
        TransactionalFlightGenerationService service = new TransactionalFlightGenerationService(
                flightRepository, routeRepository, flightBatchPersistenceService, flightGenerationExecutor
        );
        ReflectionTestUtils.setField(service, "flightGenerationDaysHorizon", 180);
        ReflectionTestUtils.setField(service, "minimumHoursBeforeDeparture", 2);
        ReflectionTestUtils.setField(service, "batchSize", 100);
        return service;
    }

    private RouteEntity createDefaultRoute() {
        return createRoute(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                Set.of(LocalTime.of(8, 0), LocalTime.of(14, 0))
        );
    }

    private RouteEntity createRoute(Set<DayOfWeek> operatingDays, Set<LocalTime> schedules) {
        AirportEntity originAirport = new AirportEntity();
        originAirport.setId(1L);
        originAirport.setIataCode("BOG");
        originAirport.setName("El Dorado");
        originAirport.setTimezone("America/Bogota");

        AirportEntity destinationAirport = new AirportEntity();
        destinationAirport.setId(2L);
        destinationAirport.setIataCode("MIA");
        destinationAirport.setName("Miami International");
        destinationAirport.setTimezone("America/New_York");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setId(1L);
        airplaneType.setModel("Boeing 737");
        airplaneType.setEconomySeats(180);

        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setFlightNumber("FA101");
        route.setAirportOrigin(originAirport);
        route.setAirportDestination(destinationAirport);
        route.setDefaultAirplaneType(airplaneType);
        route.setStatus(RouteStatus.ACTIVE);
        route.setLengthMinutes(180);
        route.setRouteDays(createRouteDays(route, operatingDays));
        route.setRouteSchedules(createRouteSchedules(route, schedules));
        return route;
    }

    private Set<RouteDayEntity> createRouteDays(RouteEntity route, Set<DayOfWeek> operatingDays) {
        Set<RouteDayEntity> routeDays = new HashSet<>();
        for (DayOfWeek dayOfWeek : operatingDays) {
            routeDays.add(new RouteDayEntity(route, dayOfWeek));
        }
        return routeDays;
    }

    private Set<RouteScheduleEntity> createRouteSchedules(RouteEntity route, Set<LocalTime> schedules) {
        Set<RouteScheduleEntity> routeSchedules = new HashSet<>();
        for (LocalTime schedule : schedules) {
            routeSchedules.add(new RouteScheduleEntity(route, schedule));
        }
        return routeSchedules;
    }

    @Nested
    @DisplayName("generateAllFlightsForRoute")
    class GenerateAllFlightsForRoute {

        private TransactionalFlightGenerationService service;
        private RouteEntity route;
        private Long routeId;

        @BeforeEach
        void setUp() {
            service = createService();
            route = createDefaultRoute();
            routeId = route.getId();
        }

        @Test
        void shouldGenerateFlightsForOperatingDays() {
            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            int totalGenerated = service.generateAllFlightsForRoute(routeId);

            assertThat(totalGenerated).isGreaterThan(0);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(anyList());
        }

        @Test
        void shouldThrowWhenRouteDoesNotExist() {
            Long invalidRouteId = 999L;
            given(routeRepository.findById(invalidRouteId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.generateAllFlightsForRoute(invalidRouteId))
                    .isInstanceOf(RouteNotFoundException.class);
        }

        @Test
        void shouldSkipExistingFlights() {
            ZoneId timezone = ZoneId.of("America/Bogota");
            LocalDate tomorrow = LocalDate.now(timezone).plusDays(1);
            OffsetDateTime existingDeparture = tomorrow.atTime(8, 0).atZone(timezone).toOffsetDateTime();

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(List.of(existingDeparture));
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean containsExistingDeparture = generatedFlights.stream()
                    .anyMatch(flight -> flight.getDepartureDateTime().equals(existingDeparture));

            assertThat(containsExistingDeparture).isFalse();
        }

        @Test
        void shouldSkipExistingFlightsWithDifferentOffsetForSameInstant() {
            ZoneId timezone = ZoneId.of("America/Bogota");
            LocalDate tomorrow = LocalDate.now(timezone).plusDays(1);
            OffsetDateTime generatedDeparture = tomorrow.atTime(8, 0).atZone(timezone).toOffsetDateTime();
            OffsetDateTime existingDeparture = generatedDeparture.withOffsetSameInstant(ZoneOffset.UTC);

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(List.of(existingDeparture));
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean containsExistingInstant = generatedFlights.stream()
                    .anyMatch(flight -> flight.getDepartureDateTime().toInstant().equals(existingDeparture.toInstant()));

            assertThat(containsExistingInstant).isFalse();
        }

        @Test
        void shouldSkipExistingFlightsOnInclusiveEndDate() {
            ZoneId timezone = ZoneId.of("America/Bogota");
            LocalDate horizonDate = LocalDate.now(timezone).plusDays(180);
            OffsetDateTime existingDeparture = horizonDate.atTime(8, 0).atZone(timezone).toOffsetDateTime();

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(List.of(existingDeparture));
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean containsEndDateDeparture = generatedFlights.stream()
                    .anyMatch(flight -> flight.getDepartureDateTime().toInstant().equals(existingDeparture.toInstant()));

            assertThat(containsEndDateDeparture).isFalse();
        }

        @Test
        void shouldRespectMinimumHoursBeforeDeparture() {
            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            OffsetDateTime minimumDeparture = OffsetDateTime.now(ZoneId.of("America/Bogota")).plusHours(2);
            boolean allFlightsRespectMinimum = generatedFlights.stream()
                    .allMatch(flight -> flight.getDepartureDateTime().isAfter(minimumDeparture));

            assertThat(allFlightsRespectMinimum).isTrue();
        }

        @Test
        void shouldReturnZeroWhenRouteHasNoOperatingDays() {
            route = createRoute(Collections.emptySet(), Set.of(LocalTime.of(8, 0), LocalTime.of(14, 0)));

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());

            int totalGenerated = service.generateAllFlightsForRoute(routeId);

            assertThat(totalGenerated).isZero();
            then(flightBatchPersistenceService).shouldHaveNoInteractions();
        }

        @Test
        void shouldReturnZeroWhenRouteHasNoSchedules() {
            route = createRoute(
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    Collections.emptySet()
            );

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());

            int totalGenerated = service.generateAllFlightsForRoute(routeId);

            assertThat(totalGenerated).isZero();
            then(flightBatchPersistenceService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("generateAllFlightsForAllRoutes")
    class GenerateAllFlightsForAllRoutes {

        private TransactionalFlightGenerationService service;
        private RouteEntity route;

        @BeforeEach
        void setUp() {
            service = createService();
            route = createDefaultRoute();
        }

        @Test
        void shouldProcessMultipleRoutes() {
            List<Long> routeIds = List.of(1L, 2L, 3L);

            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(routeIds);
            given(routeRepository.findById(anyLong())).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());
            willAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).given(flightGenerationExecutor).execute(any());

            int totalGenerated = service.generateAllFlightsForAllRoutes();

            assertThat(totalGenerated).isGreaterThan(0);
            then(routeRepository).should().findIdsByStatus(RouteStatus.ACTIVE);
        }

        @Test
        void shouldContinueWhenOneRouteFails() {
            List<Long> routeIds = List.of(1L, 2L, 3L);

            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(routeIds);
            given(routeRepository.findById(1L)).willReturn(Optional.of(route));
            given(routeRepository.findById(2L)).willReturn(Optional.empty());
            given(routeRepository.findById(3L)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());
            willAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).given(flightGenerationExecutor).execute(any());

            assertThatThrownBy(() -> service.generateAllFlightsForAllRoutes())
                    .isInstanceOf(FlightGenerationPartialFailureException.class)
                    .hasMessageContaining("2");
        }

        @Test
        void shouldReturnZeroWhenThereAreNoActiveRoutes() {
            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(Collections.emptyList());

            int totalGenerated = service.generateAllFlightsForAllRoutes();

            assertThat(totalGenerated).isZero();
            then(flightBatchPersistenceService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("generateFlightsForRouteAtHorizon")
    class GenerateFlightsForRouteAtHorizon {

        private TransactionalFlightGenerationService service;
        private Long routeId;
        private DayOfWeek horizonDay;

        @BeforeEach
        void setUp() {
            service = createService();
            routeId = 1L;
            LocalDate horizonDate = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(180);
            horizonDay = horizonDate.getDayOfWeek();
        }

        @Test
        void shouldGenerateFlightsWhenRouteOperatesThatDay() {
            RouteEntity route = createRoute(Set.of(horizonDay), Set.of(LocalTime.of(8, 0), LocalTime.of(14, 0)));

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimes(any(), anyList())).willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            int generated = service.generateFlightsForRouteAtHorizon(routeId);

            assertThat(generated).isGreaterThan(0);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(anyList());
        }

        @Test
        void shouldSkipHorizonFlightsWhenExistingDepartureHasDifferentOffset() {
            RouteEntity route = createRoute(Set.of(horizonDay), Set.of(LocalTime.of(8, 0)));
            ZoneId timezone = ZoneId.of("America/Bogota");
            LocalDate horizonDate = LocalDate.now(timezone).plusDays(180);
            OffsetDateTime generatedDeparture = horizonDate.atTime(8, 0).atZone(timezone).toOffsetDateTime();
            OffsetDateTime existingDeparture = generatedDeparture.withOffsetSameInstant(ZoneOffset.UTC);

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimes(any(), anyList())).willReturn(List.of(existingDeparture));

            int generated = service.generateFlightsForRouteAtHorizon(routeId);

            assertThat(generated).isZero();
            then(flightBatchPersistenceService).shouldHaveNoInteractions();
        }

        @Test
        void shouldReturnZeroWhenRouteDoesNotOperateThatDay() {
            DayOfWeek differentDay = horizonDay == DayOfWeek.MONDAY ? DayOfWeek.TUESDAY : DayOfWeek.MONDAY;
            RouteEntity route = createRoute(Set.of(differentDay), Set.of(LocalTime.of(8, 0), LocalTime.of(14, 0)));

            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));

            int generated = service.generateFlightsForRouteAtHorizon(routeId);

            assertThat(generated).isZero();
            then(flightBatchPersistenceService).shouldHaveNoInteractions();
        }

        @Test
        void shouldThrowWhenRouteDoesNotExist() {
            Long invalidRouteId = 999L;
            given(routeRepository.findById(invalidRouteId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.generateFlightsForRouteAtHorizon(invalidRouteId))
                    .isInstanceOf(RouteNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("generateFlightsForAllRoutesAtHorizon")
    class GenerateFlightsForAllRoutesAtHorizon {

        private TransactionalFlightGenerationService service;
        private DayOfWeek horizonDay;

        @BeforeEach
        void setUp() {
            service = createService();
            LocalDate horizonDate = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(180);
            horizonDay = horizonDate.getDayOfWeek();
        }

        @Test
        void shouldProcessAllActiveRoutes() {
            List<Long> routeIds = List.of(1L, 2L, 3L);
            RouteEntity route = createRoute(Set.of(horizonDay), Set.of(LocalTime.of(8, 0), LocalTime.of(14, 0)));

            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(routeIds);
            given(routeRepository.findById(anyLong())).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimes(any(), anyList())).willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            int totalGenerated = service.generateFlightsForAllRoutesAtHorizon();

            assertThat(totalGenerated).isGreaterThan(0);
            then(routeRepository).should().findIdsByStatus(RouteStatus.ACTIVE);
        }

        @Test
        void shouldSkipRoutesThatDoNotOperateThatDay() {
            List<Long> routeIds = List.of(1L, 2L);
            RouteEntity routeThatOperates = createRoute(Set.of(horizonDay), Set.of(LocalTime.of(8, 0)));
            routeThatOperates.setId(1L);

            DayOfWeek differentDay = horizonDay == DayOfWeek.MONDAY ? DayOfWeek.TUESDAY : DayOfWeek.MONDAY;
            RouteEntity routeThatDoesNotOperate = createRoute(Set.of(differentDay), Set.of(LocalTime.of(8, 0)));
            routeThatDoesNotOperate.setId(2L);

            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(routeIds);
            given(routeRepository.findById(1L)).willReturn(Optional.of(routeThatOperates));
            given(routeRepository.findById(2L)).willReturn(Optional.of(routeThatDoesNotOperate));
            given(flightRepository.findExistingDepartureTimes(any(), anyList())).willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            int totalGenerated = service.generateFlightsForAllRoutesAtHorizon();

            assertThat(totalGenerated).isGreaterThan(0);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(anyList());
        }

        @Test
        void shouldContinueWhenOneRouteFails() {
            List<Long> routeIds = List.of(1L, 2L, 3L);
            RouteEntity route = createDefaultRoute();

            given(routeRepository.findIdsByStatus(RouteStatus.ACTIVE)).willReturn(routeIds);
            given(routeRepository.findById(1L)).willReturn(Optional.of(route));
            given(routeRepository.findById(2L)).willThrow(new RuntimeException("Database error"));
            given(routeRepository.findById(3L)).willReturn(Optional.of(route));

            int totalGenerated = service.generateFlightsForAllRoutesAtHorizon();

            assertThat(totalGenerated).isGreaterThanOrEqualTo(0);
            assertThatCode(service::generateFlightsForAllRoutesAtHorizon).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("generated flights")
    class GeneratedFlights {

        private TransactionalFlightGenerationService service;
        private RouteEntity route;
        private Long routeId;

        @BeforeEach
        void setUp() {
            service = createService();
            route = createDefaultRoute();
            routeId = route.getId();
        }

        @Test
        void shouldCreateFlightsWithScheduledStatus() {
            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean allScheduled = generatedFlights.stream()
                    .allMatch(flight -> flight.getStatus() == FlightStatus.SCHEDULED);

            assertThat(allScheduled).isTrue();
        }

        @Test
        void shouldCreateFlightsWithTheCorrectRoute() {
            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean allBelongToRoute = generatedFlights.stream()
                    .allMatch(flight -> flight.getRoute().getId().equals(route.getId()));

            assertThat(allBelongToRoute).isTrue();
        }

        @Test
        void shouldCreateFlightsWithTheDefaultAirplaneType() {
            given(routeRepository.findById(routeId)).willReturn(Optional.of(route));
            given(flightRepository.findExistingDepartureTimesInRange(anyLong(), any(), any()))
                    .willReturn(Collections.emptyList());
            willDoNothing().given(flightBatchPersistenceService).saveBatch(anyList());

            service.generateAllFlightsForRoute(routeId);

            ArgumentCaptor<List<FlightEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
            then(flightBatchPersistenceService).should(atLeastOnce()).saveBatch(batchCaptor.capture());

            List<FlightEntity> generatedFlights = new ArrayList<>();
            batchCaptor.getAllValues().forEach(generatedFlights::addAll);
            boolean allUseDefaultAirplane = generatedFlights.stream()
                    .allMatch(flight -> flight.getAirplaneType().getId().equals(route.getDefaultAirplaneType().getId()));

            assertThat(allUseDefaultAirplane).isTrue();
        }
    }
}
