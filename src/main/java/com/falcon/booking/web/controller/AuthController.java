package com.falcon.booking.web.controller;

import com.falcon.booking.security.service.AuthService;
import com.falcon.booking.web.dto.auth.LoginRequestDto;
import com.falcon.booking.web.dto.auth.LoginResponseDto;
import com.falcon.booking.web.dto.user.CreateUserDto;
import com.falcon.booking.web.exception.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User login",
            description = "Authenticates a user and returns a JWT token if the credentials are valid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid
                                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                          description = "User login credentials",
                                                          required = true)
                                                  LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    @Operation(summary = "Register a new client user",
            description = "Creates a new client user account with the provided registration data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Client user registered successfully"),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or user already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerClient(@RequestBody @Valid
                                               @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                       description = "Data for registering a new client user",
                                                       required = true)
                                               CreateUserDto requestDto) {
        authService.registerClient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Register a new admin user",
            description = "Creates a new admin user account with the provided registration data. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin user registered successfully"),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or user already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to register admin user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/register-admin")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid
                                              @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                      description = "Data for registering a new admin user",
                                                      required = true)
                                              CreateUserDto requestDto) {
        authService.registerAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
