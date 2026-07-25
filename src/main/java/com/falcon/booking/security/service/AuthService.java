package com.falcon.booking.security.service;

import com.falcon.booking.feature.auth.dto.CreateUserDto;
import com.falcon.booking.feature.auth.dto.LoginRequestDto;
import com.falcon.booking.feature.auth.dto.LoginResponseDto;
import com.falcon.booking.feature.auth.dto.PasswordResetRequestDto;
import com.falcon.booking.feature.auth.dto.ResetPasswordDto;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.auth.service.PasswordResetService;
import com.falcon.booking.security.jwt.JwtPayload;
import com.falcon.booking.security.jwt.JwtUtil;
import com.falcon.booking.security.model.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        JwtPayload jwtPayload = new JwtPayload(user.getId(), user.getEmail(), user.getStringAuthorities());
        String token = jwtUtil.generateToken(jwtPayload);

        return new LoginResponseDto("Bearer", token);
    }

    @Transactional
    public void registerClient(CreateUserDto request) {
         userService.createClientUser(request);
    }

    @Transactional
    public void registerAdmin(CreateUserDto request) {
        userService.createAdminUser(request);
    }

    public void requestPasswordReset(PasswordResetRequestDto request) {
        passwordResetService.requestPasswordReset(request.email());
    }

    public void resetPassword(ResetPasswordDto request) {
        passwordResetService.resetPassword(request.code(), request.password());
    }
}
