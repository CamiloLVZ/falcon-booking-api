package com.falcon.booking.feature.admin.controller;

import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.admin.dto.AdminUserDto;
import com.falcon.booking.feature.admin.dto.UpdateUserCredentialsDto;
import com.falcon.booking.feature.admin.service.AdminUserService;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.service.ReservationQueryService;
import com.falcon.booking.persistence.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Administrative operations (ADMIN role required)")
@RestController
@RequestMapping("/v1/admin")
@Validated
public class AdminController {

    private final AdminUserService adminUserService;
    private final UserService userService;
    private final ReservationQueryService reservationQueryService;

    public AdminController(AdminUserService adminUserService,
                           UserService userService,
                           ReservationQueryService reservationQueryService) {
        this.adminUserService = adminUserService;
        this.userService = userService;
        this.reservationQueryService = reservationQueryService;
    }

    @Operation(summary = "List all users",
            description = "Returns a paginated list of all users with optional filters. Includes passenger profile if exists. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserDto>> getAllUsers(
            @RequestParam(required = false)
            @Parameter(description = "Filter by email (partial match)", example = "john")
            String email,
            @RequestParam(required = false)
            @Parameter(description = "Filter by disabled status", example = "false")
            Boolean disabled,
            @RequestParam(required = false)
            @Parameter(description = "Filter by role name", example = "CLIENT")
            String role,
            @RequestParam @Min(1) @NotNull
            @Parameter(description = "Number of records per page", example = "10", required = true)
            int size,
            @RequestParam @Min(0) @NotNull
            @Parameter(description = "Zero-based page number", example = "0", required = true)
            int page) {
        Page<AdminUserDto> users = adminUserService.getAllUsers(email, disabled, role, page, size);
        return ResponseEntity.ok(PagedResponse.from(users));
    }

    @Operation(summary = "List reservations of a user",
            description = "Returns a paginated list of reservations belonging to a specific user, with optional status filter. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated reservations retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<PagedResponse<ResponseReservationDto>> getUserReservations(
            @PathVariable
            @Parameter(description = "User numeric unique identifier", example = "1")
            Long userId,
            @RequestParam(required = false)
            @Parameter(description = "Optional filter by reservation status", example = "RESERVED")
            ReservationStatus status,
            @RequestParam @Min(1) @NotNull
            @Parameter(description = "Number of records per page", example = "10", required = true)
            int size,
            @RequestParam @Min(0) @NotNull
            @Parameter(description = "Zero-based page number", example = "0", required = true)
            int page) {
        UserEntity user = userService.getUserById(userId);
        Page<ResponseReservationDto> reservations = reservationQueryService.getMyReservations(user, status, page, size);
        return ResponseEntity.ok(PagedResponse.from(reservations));
    }

    @Operation(summary = "Enable or disable a user",
            description = "Toggles the disabled status of a user account. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User disabled status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/users/{userId}/toggle-disabled")
    public ResponseEntity<Void> toggleUserDisabled(
            @PathVariable
            @Parameter(description = "User numeric unique identifier", example = "1")
            Long userId) {
        adminUserService.toggleUserDisabled(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Update user email or password",
            description = "Updates the email and/or password of a user. At least one field must be provided. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User credentials updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or no fields provided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/users/{userId}/credentials")
    public ResponseEntity<Void> updateUserCredentials(
            @PathVariable
            @Parameter(description = "User numeric unique identifier", example = "1")
            Long userId,
            @Valid @RequestBody UpdateUserCredentialsDto dto) {
        adminUserService.updateUserCredentials(userId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
