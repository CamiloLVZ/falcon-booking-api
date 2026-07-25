package com.falcon.booking.feature.passenger.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.security.jwt.JwtPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Profile", description = "Operations for the authenticated client to manage their personal passenger profile")
@RestController
@RequestMapping("/v1/passengers/me")
public class UserProfileController {

    private final PassengerService passengerService;
    private final UserService userService;

    public UserProfileController(PassengerService passengerService, UserService userService) {
        this.passengerService = passengerService;
        this.userService = userService;
    }

    @Operation(summary = "Get my passenger profile",
            description = "Returns the passenger profile linked to the authenticated client account.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "No passenger profile linked to this account",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<ResponsePassengerDto> getMyProfile(Authentication authentication) {
        UserEntity user = resolveUser(authentication);
        return ResponseEntity.ok(passengerService.getMyProfile(user));
    }

    @Operation(summary = "Create my passenger profile",
            description = "Creates and links a passenger profile to the authenticated client account. Only allowed once; use PUT to update.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Profile already linked to this account",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponsePassengerDto> createMyProfile(@Valid @RequestBody AddPassengerDto requestDto,
                                                                Authentication authentication) {
        UserEntity user = resolveUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.createMyProfile(user, requestDto));
    }

    @Operation(summary = "Update my passenger profile",
            description = "Updates the passenger profile linked to the authenticated client account.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "No passenger profile linked to this account",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping
    public ResponseEntity<ResponsePassengerDto> updateMyProfile(@Valid @RequestBody AddPassengerDto requestDto,
                                                                Authentication authentication) {
        UserEntity user = resolveUser(authentication);
        return ResponseEntity.ok(passengerService.updateMyProfile(user, requestDto));
    }

    private UserEntity resolveUser(Authentication authentication) {
        JwtPayload payload = (JwtPayload) authentication.getPrincipal();
        return userService.getUserById(payload.userId());
    }
}
