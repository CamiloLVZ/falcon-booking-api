package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
@Validated
public class FlightController {

    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseFlightDto> getFlightById(@PathVariable Long id){
        return ResponseEntity.ok(flightService.getFlightById(id));
    }


    @PostMapping
    public ResponseEntity<ResponseFlightDto> addFlight(@RequestBody @Valid CreateFlightDto createFlightDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.addFlight(createFlightDto));
    }

}
