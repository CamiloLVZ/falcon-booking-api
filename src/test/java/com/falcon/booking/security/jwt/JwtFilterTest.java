package com.falcon.booking.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void cleanContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("Should skip jwt validation for auth endpoints")
    @Test
    void shouldSkipJwtValidation_doFilterInternal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("Should continue filter chain when authorization header is missing")
    @Test
    void shouldContinueWithoutAuthentication_doFilterInternal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/flights");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("Should set authentication when bearer token is valid")
    @Test
    void shouldSetAuthentication_doFilterInternal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/flights/generations");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JwtPayload payload = new JwtPayload(22L, "admin@test.com", List.of("ADMIN"));
        given(jwtUtil.extractPayload("valid-token")).willReturn(payload);

        jwtFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(payload);
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        verify(jwtUtil).extractPayload("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("Should continue without authentication when jwt verification fails")
    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsInvalid_doFilterInternal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/flights");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.extractPayload("invalid-token"))
                .willThrow(new JWTVerificationException("Token invalid"));

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil).extractPayload("invalid-token");
        verify(filterChain).doFilter(request, response);
    }
}
