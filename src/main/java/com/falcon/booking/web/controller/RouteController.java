package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.RouteService;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.Route.CreateRouteDto;
import com.falcon.booking.web.dto.Route.ResponseRouteDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
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
    public ResponseEntity<ResponseRouteDto> getRouteByFlightNumber(@PathVariable String flightNumber){
        return ResponseEntity.ok(routeService.getRouteByFlightNumber(flightNumber));
    }

    @PostMapping
    public ResponseEntity<ResponseRouteDto> addRoute(@RequestBody @Valid CreateRouteDto createRouteDto){
        return ResponseEntity.ok(routeService.addRoute(createRouteDto));
    }


}
