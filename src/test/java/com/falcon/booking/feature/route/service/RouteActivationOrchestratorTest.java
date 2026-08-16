package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RouteActivationOrchestratorTest {

    @Mock
    private RouteCommandService routeCommandService;

    @Mock
    private FlightGenerationService flightGenerationService;

    @InjectMocks
    private RouteActivationOrchestrator routeActivationOrchestrator;

    @DisplayName("Should activate route and start flight generation when synchronization is not active")
    @Test
    void shouldActivateRouteWithoutActiveTransactionSynchronization() {
        String flightNumber = "AV1234";
        ResponseRouteDto expectedResponse = mock(ResponseRouteDto.class);
        given(routeCommandService.activateRoute(flightNumber)).willReturn(expectedResponse);

        ResponseRouteDto actualResponse = routeActivationOrchestrator.activateRoute(flightNumber);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(routeCommandService).activateRoute(flightNumber);
        verify(flightGenerationService).startRouteFlightGeneration(flightNumber);
    }

    @DisplayName("Should register synchronization and trigger flight generation after commit when synchronization is active")
    @Test
    void shouldActivateRouteWithActiveTransactionSynchronization() {
        String flightNumber = "AV1234";
        ResponseRouteDto expectedResponse = mock(ResponseRouteDto.class);
        given(routeCommandService.activateRoute(flightNumber)).willReturn(expectedResponse);

        TransactionSynchronizationManager.initSynchronization();
        try {
            ResponseRouteDto actualResponse = routeActivationOrchestrator.activateRoute(flightNumber);

            assertThat(actualResponse).isEqualTo(expectedResponse);
            verify(routeCommandService).activateRoute(flightNumber);

            // Execute registered afterCommit callbacks
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            verify(flightGenerationService).startRouteFlightGeneration(flightNumber);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
