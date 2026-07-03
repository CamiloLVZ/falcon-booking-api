package com.falcon.booking.feature.flightGeneration.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Flight Generation queries", description = "Operations related to flight generations querying")
@RestController
@RequestMapping("/v1/flights")
@Validated
public class FlightGenerationController {


    private final FlightGenerationService flightGenerationService;

    public FlightGenerationController(FlightGenerationService flightGenerationService) {
        this.flightGenerationService = flightGenerationService;
    }

    @Operation(summary = "Get all flight generations",
            description = "Returns a paginated list of historic flight generations. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated flight generations retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve flight generations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class)))
    })
    @GetMapping("/generations")
    public ResponseEntity<PagedResponse<ResponseFlightsGenerationDto>> getAllFlightGenerations(@RequestParam @Min(1) @NotNull
                                                                                               @Parameter(description = "Number of flight generation records to be returned per page", example = "10", required = true)
                                                                                               int size,
                                                                                               @RequestParam @Min(0) @NotNull
                                                                                               @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                                               int page) {
        Page<ResponseFlightsGenerationDto> generations = flightGenerationService.getAllFlightGenerations(page, size);
        return ResponseEntity.ok(PagedResponse.from(generations));
    }

    @Operation(summary = "Get a flight generation by id",
            description = "Returns a flight generation process record using its unique numeric identifier. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight generation retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightsGenerationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid id argument",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve flight generations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.falcon.booking.common.web.Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight generation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/generations/{id}")
    public ResponseEntity<ResponseFlightsGenerationDto> getFlightsGeneration(@PathVariable
                                                                             @Parameter(description = "Flights generation unique identifier.", example = "10")
                                                                             Long id) {
        return ResponseEntity.ok(flightGenerationService.getFlightGeneration(id));
    }


}
