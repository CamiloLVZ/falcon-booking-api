package com.falcon.booking.integration;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.auth.dto.CreateUserDto;
import com.falcon.booking.feature.auth.dto.LoginRequestDto;
import com.falcon.booking.feature.auth.dto.LoginResponseDto;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AdminIT extends BaseIntegrationTest {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final String ADMIN_EMAIL = "admin-it@falcon.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired private UserService userService;
    @Autowired private AirplaneTypeRepository airplaneTypeRepository;

    private String adminJwt;
    private Long airplaneTypeId;

    @BeforeEach
    void setUp() {
        userService.createAdminIfNotExists(new CreateUserDto(ADMIN_EMAIL, ADMIN_PASSWORD));

        ResponseEntity<LoginResponseDto> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/login",
                new LoginRequestDto(ADMIN_EMAIL, ADMIN_PASSWORD),
                LoginResponseDto.class);
        adminJwt = loginResponse.getBody().accessToken();

        AirplaneTypeEntity plane = new AirplaneTypeEntity();
        plane.setProducer("Boeing");
        plane.setModel("737-ADMIN-" + counter.incrementAndGet());
        plane.configureSeats(168, 12, "ABC");
        plane.setStatus(AirplaneTypeStatus.ACTIVE);
        plane = airplaneTypeRepository.save(plane);
        airplaneTypeId = plane.getId();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminJwt);
        return headers;
    }

    @Test
    @DisplayName("Admin should create a new route")
    void createRoute_AsAdmin_ShouldReturn201() {
        int id = counter.incrementAndGet();
        var request = new CreateRouteDto(
                "ADM" + (100 + id), "BOG", "MDE", airplaneTypeId, 60,
                new BigDecimal("100.00"), new BigDecimal("200.00")
        );

        ResponseEntity<ResponseRouteDto> response = restTemplate.exchange(
                baseUrl() + "/v1/routes",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                ResponseRouteDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().flightNumber()).isNotBlank();
        assertThat(response.getBody().status()).isNotNull();
    }

    @Test
    @DisplayName("Admin should create a new flight on an existing route")
    void createFlight_AsAdmin_ShouldReturn201() {
        int id = counter.incrementAndGet();
        String flightNumber = "ADM" + (200 + id);

        CreateRouteDto routeReq = new CreateRouteDto(
                flightNumber, "BOG", "MDE", airplaneTypeId, 90,
                new BigDecimal("150.00"), new BigDecimal("300.00")
        );
        restTemplate.exchange(baseUrl() + "/v1/routes", HttpMethod.POST,
                new HttpEntity<>(routeReq, authHeaders()), ResponseRouteDto.class);

        var flightReq = new CreateFlightDto(flightNumber, LocalDateTime.now().plusDays(15));

        ResponseEntity<ResponseFlightDto> response = restTemplate.exchange(
                baseUrl() + "/v1/flights",
                HttpMethod.POST,
                new HttpEntity<>(flightReq, authHeaders()),
                ResponseFlightDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().flightNumber()).isEqualTo(flightNumber);
    }

    @Test
    @DisplayName("Should return 403 when accessing admin endpoint without authentication")
    void adminEndpoint_WithoutAuth_ShouldReturn403() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/v1/flights/generations?size=10&page=0", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
