package com.falcon.booking.security.service;

import com.falcon.booking.domain.service.UserService;
import com.falcon.booking.security.jwt.JwtPayload;
import com.falcon.booking.security.jwt.JwtUtil;
import com.falcon.booking.security.model.CustomUserDetails;
import com.falcon.booking.web.dto.auth.LoginRequestDto;
import com.falcon.booking.web.dto.auth.LoginResponseDto;
import com.falcon.booking.web.dto.user.CreateUserDto;
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

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
}
