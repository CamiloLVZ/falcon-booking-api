package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.web.dto.AirportDto;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airports")
@Validated
public class AirportController {

    private final AirportService airportService;

    @Autowired
    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<AirportDto> getAirport(@PathVariable @Size(min = 3, max = 3, message = "Iata Code must be a String with 3 characters")
                                                     String iataCode) {
        return ResponseEntity.ok(airportService.getAirportByIataCode(iataCode));
    }

    @GetMapping
    public ResponseEntity<List<AirportDto>> getAirports() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }



}
