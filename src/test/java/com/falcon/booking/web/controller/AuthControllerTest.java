package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.User.UserAlreadyExistException;
import com.falcon.booking.security.jwt.JwtFilter;
import com.falcon.booking.security.service.AuthService;
import com.falcon.booking.web.dto.auth.LoginRequestDto;
import com.falcon.booking.web.dto.auth.LoginResponseDto;
import com.falcon.booking.web.dto.user.CreateUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @DisplayName("Should return 200 OK and token when credentials are valid")
    @Test
    void shouldReturn200AndToken_login() throws Exception {
        LoginResponseDto expectedResponse = new LoginResponseDto("Bearer", "fake-jwt-token");
        given(authService.login(new LoginRequestDto("client@test.com", "password123")))
                .willReturn(expectedResponse);

        ResultActions response = mockMvc.perform(post("/v1/auth/login")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "client@test.com",
                          "password": "password123"
                        }
                        """));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("fake-jwt-token"));
    }

    @DisplayName("Should return 401 unauthorized when credentials are invalid")
    @Test
    void shouldReturn401Unauthorized_login() throws Exception {
        given(authService.login(new LoginRequestDto("client@test.com", "wrong-password")))
                .willThrow(new BadCredentialsException("Bad credentials"));

        ResultActions response = mockMvc.perform(post("/v1/auth/login")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "client@test.com",
                          "password": "wrong-password"
                        }
                        """));

        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("invalid-credentials"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when bad request to login")
    @Test
    void shouldReturn400InvalidArguments_login() throws Exception {
        ResultActions response = mockMvc.perform(post("/v1/auth/login")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "abc",
                          "password": "123"
                        }
                        """));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].message").exists());
    }

    @DisplayName("Should return 201 created when register client is successful")
    @Test
    void shouldReturn201Created_registerClient() throws Exception {
        doNothing().when(authService).registerClient(new CreateUserDto("new.client@test.com", "password123"));

        ResultActions response = mockMvc.perform(post("/v1/auth/register")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "new.client@test.com",
                          "password": "password123"
                        }
                        """));

        response.andExpect(status().isCreated());
    }

    @DisplayName("Should return 409 conflict when user already exists on register client")
    @Test
    void shouldReturn409Conflict_registerClient() throws Exception {
        willThrow(new UserAlreadyExistException("existing@test.com"))
                .given(authService).registerClient(new CreateUserDto("existing@test.com", "password123"));

        ResultActions response = mockMvc.perform(post("/v1/auth/register")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "existing@test.com",
                          "password": "password123"
                        }
                        """));

        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("user-already-exists"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 201 created when register admin is successful")
    @Test
    void shouldReturn201Created_registerAdmin() throws Exception {
        doNothing().when(authService).registerAdmin(new CreateUserDto("new.admin@test.com", "password123"));

        ResultActions response = mockMvc.perform(post("/v1/auth/register-admin")
               .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "new.admin@test.com",
                          "password": "password123"
                        }
                        """));

        response.andExpect(status().isCreated());
    }
}



