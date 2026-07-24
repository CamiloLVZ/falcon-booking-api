package com.falcon.booking.feature.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "Health check endpoints")
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    @Operation(summary = "Check API health", description = "Returns HTTP 200 OK status to indicate that the API is up and running.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is healthy")
    })
    @GetMapping
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
