package com.falcon.booking.integration;

import com.falcon.booking.feature.auth.dto.CreateUserDto;
import com.falcon.booking.feature.auth.dto.LoginRequestDto;
import com.falcon.booking.feature.auth.dto.LoginResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Should register a new client user")
    void registerClient_ShouldReturn201() {
        String email = "client-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        var request = new CreateUserDto(email, "password123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Should return 400 when registering a duplicate email")
    void registerClient_DuplicateEmail_ShouldReturn400() {
        String email = "dup-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        var request = new CreateUserDto(email, "password123");

        restTemplate.postForEntity(baseUrl() + "/v1/auth/register", request, String.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should login with valid credentials and return JWT")
    void login_ValidCredentials_ShouldReturn200() {
        String email = "login-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        restTemplate.postForEntity(baseUrl() + "/v1/auth/register",
                new CreateUserDto(email, "password123"), String.class);

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/login",
                new LoginRequestDto(email, "password123"),
                LoginResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("Should return 401 when login with invalid credentials")
    void login_InvalidCredentials_ShouldReturn401() {
        var request = new LoginRequestDto("nonexistent@test.com", "wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
