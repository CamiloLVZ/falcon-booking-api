package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirplaneTypeService;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import com.falcon.booking.web.exception.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Airplane Types", description = "All methods for airplane types management")
@RestController
@RequestMapping("/v1/airplane-types")
public class AirplaneTypeController {

    private final AirplaneTypeService airplaneTypeService;

    @Autowired
    public AirplaneTypeController(AirplaneTypeService airplaneTypeService) {
        this.airplaneTypeService = airplaneTypeService;
    }

    @Operation(summary = "Get an airplane type by id",
            description = "Returns an airplane type record using its unique identifier. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid argument type",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseAirplaneTypeDto> getAirplaneTypeById(@PathVariable
                                                                       @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                       Long id) {
        return ResponseEntity.ok(airplaneTypeService.getAirplaneTypeById(id));
    }

    @Operation(summary = "Get all airplane types",
            description = "Returns a list with all registered airplane types with optional arguments to filter. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane types list retrieved successfully, even if it is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseAirplaneTypeDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<List<ResponseAirplaneTypeDto>> getAllAirplanesType(@RequestParam(required = false)
                                                                             @Parameter(description = "Airplane type producers name", example = "AIRBUS")
                                                                             String producer,
                                                                             @RequestParam(required = false)
                                                                             @Parameter(description = "Airplane type model name", example = "320-200")
                                                                             String model,
                                                                             @RequestParam(required = false)
                                                                             @Parameter(description = "Airplane type status", example = "ACTIVE")
                                                                             AirplaneTypeStatus status) {

        return ResponseEntity.ok(airplaneTypeService.getAirplaneTypes(producer, model, status));
    }

    @Operation(summary = "Create a new airplane type",
            description = "Creates a new airplane type record and returns its data. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Airplane type created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to create airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponseAirplaneTypeDto> createAirplaneType(@RequestBody @Valid
                                                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                              description = "Data for creating a new airplane type",
                                                                              required = true
                                                                      )
                                                                      CreateAirplaneTypeDto createAirplaneTypeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                airplaneTypeService.addAirplaneType(createAirplaneTypeDto)
        );
    }

    @Operation(summary = "Update an airplane type seat quantities",
            description = "Finds an airplane type record by its id and allows update its seats quantity. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseAirplaneTypeDto> updateAirplaneType(@PathVariable
                                                                      @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                      Long id,
                                                                      @Valid @RequestBody
                                                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                              description = "Data for updating an existing airplane type record",
                                                                              required = true
                                                                      )
                                                                      UpdateAirplaneTypeDto updateAirplaneTypeDto) {
        return ResponseEntity.ok(
                airplaneTypeService.updateAirplaneType(id, updateAirplaneTypeDto)
        );
    }

    @Operation(summary = "Update an airplane type identity",
            description = "Finds an airplane type record by its id and allows update its producer and model names. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{id}/correct-identity")
    public ResponseEntity<ResponseAirplaneTypeDto> correctAirplaneType(@PathVariable
                                                                       @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                       Long id,
                                                                       @Valid @RequestBody
                                                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                               description = "Data for updating an airplane type record identity",
                                                                               required = true
                                                                       )
                                                                       CorrectAirplaneTypeDto correctAirplaneTypeDto) {
        return ResponseEntity.ok(
                airplaneTypeService.correctAirplaneType(id, correctAirplaneTypeDto)
        );
    }

    @Operation(summary = "Deactivate an airplane type",
            description = "Finds an airplane type record by its id and changes its status to INACTIVE. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type deactivated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to deactivate airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseAirplaneTypeDto> deactivateAirplaneType(@PathVariable
                                                                          @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                          Long id) {
        return ResponseEntity.ok(airplaneTypeService.deactivateAirplaneType(id));
    }

    @Operation(summary = "Activate an airplane type",
            description = "Finds an airplane type record by its id and changes its status to ACTIVE. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to activate airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseAirplaneTypeDto> activateAirplaneType(@PathVariable
                                                                        @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                        Long id) {
        return ResponseEntity.ok(airplaneTypeService.activateAirplaneType(id));
    }

    @Operation(summary = "Retire an airplane type",
            description = "Finds an airplane type record by its id and changes its status to RETIRED. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane type retired successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAirplaneTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retire airplane types",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{id}/retire")
    public ResponseEntity<ResponseAirplaneTypeDto> retireAirplaneType(@PathVariable
                                                                      @Parameter(description = "Airplane type numeric unique identifier ", example = "10")
                                                                      Long id) {
        return ResponseEntity.ok(airplaneTypeService.retireAirplaneType(id));
    }

}
