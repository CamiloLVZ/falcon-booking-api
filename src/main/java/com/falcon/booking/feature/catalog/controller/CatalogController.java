package com.falcon.booking.feature.catalog.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.catalog.dto.CatalogDropdownDto;
import com.falcon.booking.feature.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalog", description = "Catalog data for frontend dropdowns and search selectors")
@RestController
@RequestMapping("/v1/catalog")
@Validated
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Get dropdown options", description = "Returns all airports and airplane types for frontend dropdowns. If the user has ADMIN role, all airplane types are returned; otherwise only ACTIVE ones.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dropdown options retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogDropdownDto.class)))
    })
    @GetMapping("/dropdown-options")
    public ResponseEntity<CatalogDropdownDto> getDropdownOptions(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(catalogService.getDropdownOptions(isAdmin));
    }

    @Operation(summary = "Get origin airports for search",
            description = "Returns a list of airports with active routes as origin options for route search filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Origin airport options retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AirportSearchOptionDto.class))))
    })
    @GetMapping("/origin-airports")
    public ResponseEntity<List<AirportSearchOptionDto>> getOriginAirports() {
        return ResponseEntity.ok(catalogService.getOriginAirports());
    }

    @Operation(summary = "Get destination airports for search",
            description = "Returns a list of destination airports with active routes from the selected origin airport.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Destination airport options retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AirportSearchOptionDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid origin IATA code",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/destination-airports")
    public ResponseEntity<List<AirportSearchOptionDto>> getDestinationAirports(@RequestParam
                                                                               @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                                               @Parameter(description = "Origin airport IATA code used to find available destinations", example = "BOG")
                                                                               String originIataCode) {
        return ResponseEntity.ok(catalogService.getDestinationAirports(originIataCode));
    }
}