package com.falcon.booking.security.service;

import com.falcon.booking.domain.service.UserService;
import com.falcon.booking.security.jwt.JwtPayload;
import com.falcon.booking.security.jwt.JwtUtil;
import com.falcon.booking.security.model.CustomUserDetails;
import com.falcon.booking.web.dto.auth.LoginRequestDto;
import com.falcon.booking.web.dto.auth.LoginResponseDto;
import com.falcon.booking.web.dto.user.CreateUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @DisplayName("Should return LoginResponseDto with bearer token when login is successful")
    @Test
    void shouldReturnLoginResponse_login() {
        LoginRequestDto request = new LoginRequestDto("client@test.com", "password123");
        CustomUserDetails userDetails = new CustomUserDetails(
                5L,
                "client@test.com",
                "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtUtil.generateToken(new JwtPayload(5L, "client@test.com", List.of("CLIENT"))))
                .willReturn("token-123");

        LoginResponseDto result = authService.login(request);

        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessToken()).isEqualTo("token-123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(new JwtPayload(5L, "client@test.com", List.of("CLIENT")));
    }

    @DisplayName("Should call createClientUser when register client is requested")
    @Test
    void shouldCallCreateClientUser_registerClient() {
        CreateUserDto request = new CreateUserDto("new.client@test.com", "password123");

        authService.registerClient(request);

        verify(userService).createClientUser(request);
    }

    @DisplayName("Should call createAdminUser when register admin is requested")
    @Test
    void shouldCallCreateAdminUser_registerAdmin() {
        CreateUserDto request = new CreateUserDto("new.admin@test.com", "password123");

        authService.registerAdmin(request);

        verify(userService).createAdminUser(request);
    }
}
