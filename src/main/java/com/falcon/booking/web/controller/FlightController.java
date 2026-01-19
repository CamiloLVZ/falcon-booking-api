package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ResponseFlightDto>> getAllFlights(@RequestParam @NotNull
                                                                 @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                 String flightNumber,
                                                                 @RequestParam(required = false)
                                                                 FlightStatus status,
                                                                 @RequestParam (required = false)
                                                                     LocalDate dateFrom,
                                                                 @RequestParam (required = false)
                                                                     LocalDate dateTo
                                                                 ){
        return ResponseEntity.ok(flightService.getAllFlights(flightNumber, status, dateFrom, dateTo));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<ResponseFlightDto> rescheduleFlight(@PathVariable Long id,
                                                             @RequestParam @Future
                                                             LocalDateTime newDepartureLocalDateTime){
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.rescheduleFLight(id, newDepartureLocalDateTime));
    }

    @PostMapping
    public ResponseEntity<ResponseFlightDto> addFlight(@RequestBody @Valid CreateFlightDto createFlightDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.addFlight(createFlightDto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ResponseFlightDto> cancelFlight(@PathVariable Long id){
        return ResponseEntity.ok(flightService.cancelFlight(id));
    }

    @PatchMapping("/{id}/change-airplane-type")
    public ResponseEntity<ResponseFlightDto> cancelFlight(@PathVariable Long id,
                                                          @RequestParam Long idAirplaneType){
        return ResponseEntity.ok(flightService.changeAirplaneType(id, idAirplaneType));
    }


}
