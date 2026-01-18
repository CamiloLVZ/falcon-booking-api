package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.service.RouteService;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import com.falcon.booking.web.dto.route.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;
    private final FlightService flightService;

    @Autowired
    public RouteController(RouteService routeService, FlightService flightService) {
        this.routeService = routeService;
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseRouteDto>> getAllRoutes(@RequestParam(required = false) @Size(min = 3,max = 3, message = "Iata Code must be a 3 letter String")
                                                         String originAirportIataCode,
                                                               @RequestParam(required = false) @Size(min = 3,max = 3, message = "Iata Code must be a 3 letter String")
                                                         String destinationAirportIataCode,
                                                               @RequestParam(required = false)
                                                         RouteStatus status){

        return ResponseEntity.ok(routeService.getAllRoutes(originAirportIataCode, destinationAirportIataCode, status));

    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<ResponseRouteDto> getRouteByFlightNumber(@PathVariable
                                                                       @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                       String flightNumber){
        return ResponseEntity.ok(routeService.getRouteByFlightNumber(flightNumber));
    }

    @PostMapping
    public ResponseEntity<ResponseRouteDto> addRoute(@RequestBody @Valid CreateRouteDto createRouteDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.addRoute(createRouteDto));
    }

    @PutMapping("/{flightNumber}")
    public ResponseEntity<ResponseRouteDto> updateRoute(@PathVariable  @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                            String flightNumber,
                                                        @RequestBody @Valid UpdateRouteDto updateRouteDto){
        return ResponseEntity.ok(routeService.updateRoute(flightNumber, updateRouteDto));
    }

    @PatchMapping("/{flightNumber}/activate")
    public ResponseEntity<ResponseRouteDto> activateDto(@PathVariable
                                                            @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                            String flightNumber){
        return ResponseEntity.ok(routeService.activateRoute(flightNumber));
    }
    @PatchMapping("/{flightNumber}/deactivate")
    public ResponseEntity<ResponseRouteDto> deactivateDto(@PathVariable
                                                        @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                        String flightNumber){
        return ResponseEntity.ok(routeService.deactivateRoute(flightNumber));
    }


    @PatchMapping("/{flightNumber}/schedules")
    public ResponseEntity<RouteWithSchedulesDto> setRouteOperatingSchedules(@PathVariable
                                                         @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                         String flightNumber,
                                                              @RequestBody AddRouteScheduleRequestDto schedules){
        return ResponseEntity.ok(routeService.setRouteOperatingSchedules(flightNumber, schedules));
    }

    @GetMapping("/{flightNumber}/schedules")
    public ResponseEntity<RouteWithSchedulesDto> getRouteSchedules(@PathVariable
                                                                       @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                       String flightNumber){
        return ResponseEntity.ok(routeService.getRouteWithSchedules(flightNumber));
    }


    @PostMapping("/{flightNumber}/generateFlights")
    public ResponseEntity<ResponseFlightsGeneratedDto> generateFlightsForRoute(@PathVariable
                                                                                   @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                                   String flightNumber){

        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.generateFlightsForRoute(flightNumber));
    }

    @PostMapping("/generateFlights")
    public ResponseEntity<List<ResponseFlightsGeneratedDto>> generateFlightForAllRoutes(){
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.generateFlightsForAllRoutes());
    }


}
