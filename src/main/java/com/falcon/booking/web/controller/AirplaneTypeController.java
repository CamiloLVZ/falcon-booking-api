package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirplaneTypeService;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airplane-types")
public class AirplaneTypeController {

    private final AirplaneTypeService airplaneTypeService;

    @Autowired
    public AirplaneTypeController(AirplaneTypeService airplaneTypeService) {
        this.airplaneTypeService = airplaneTypeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseAirplaneTypeDto> getAirplaneTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(airplaneTypeService.getAirplaneTypeById(id));
    }

    @GetMapping
    public ResponseEntity<List<ResponseAirplaneTypeDto>> getAllAirplanesType(@RequestParam(required = false) String producer,
                                                                             @RequestParam(required = false) String model,
                                                                             @RequestParam(required = false) AirplaneTypeStatus status) {

        return ResponseEntity.ok(airplaneTypeService.getAirplaneTypes(producer, model, status));
    }

    @PostMapping
    public ResponseEntity<ResponseAirplaneTypeDto> createAirplaneType(@RequestBody @Valid CreateAirplaneTypeDto createAirplaneTypeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                airplaneTypeService.addAirplaneType(createAirplaneTypeDto)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseAirplaneTypeDto> updateAirplaneType(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateAirplaneTypeDto updateAirplaneTypeDto) {
        return ResponseEntity.ok(
                airplaneTypeService.updateAirplaneType(id, updateAirplaneTypeDto)
        );
    }

    @PutMapping("/{id}/correct-identity")
    public ResponseEntity<ResponseAirplaneTypeDto> correctAirplaneType(@PathVariable Long id,
                                                                       @Valid @RequestBody CorrectAirplaneTypeDto correctAirplaneTypeDto) {
        return ResponseEntity.ok(
                airplaneTypeService.correctAirplaneType(id, correctAirplaneTypeDto)
        );
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseAirplaneTypeDto> deactivateAirplaneType(@PathVariable Long id) {
        return ResponseEntity.ok(airplaneTypeService.deactivateAirplaneType(id));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseAirplaneTypeDto> activateAirplaneType(@PathVariable Long id) {
        return ResponseEntity.ok(airplaneTypeService.activateAirplaneType(id));
    }

    @PutMapping("/{id}/retire")
    public ResponseEntity<ResponseAirplaneTypeDto> retireAirplaneType(@PathVariable Long id) {
        return ResponseEntity.ok(airplaneTypeService.retireAirplaneType(id));
    }

}
