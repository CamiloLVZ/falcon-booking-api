package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.PassengerService;
import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passengers")
@Validated
public class PassengerController {

    private final PassengerService passengerService;
    private final ReservationService reservationService;

    @Autowired
    public PassengerController(PassengerService passengerService, ReservationService reservationService) {
        this.passengerService = passengerService;
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsePassengerDto> getPassengerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @GetMapping("/identification")
    public ResponseEntity<ResponsePassengerDto> getPassengerByPassportNumber(@RequestParam @NotBlank String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode) {
        return ResponseEntity.ok(passengerService.getPassengerByIdentificationNumber(identificationNumber, countryIsoCode));
    }

    @GetMapping("/passport/{passportNumber}")
    public ResponseEntity<ResponsePassengerDto> getPassengerByPassportNumber(@PathVariable("passportNumber") String passportNumber) {
        return ResponseEntity.ok(passengerService.getPassengerByPassportNumber(passportNumber));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ResponseReservationDto>> getAllReservationByPassenger(@RequestParam @NotBlank String identificationNumber,
                                                                                  @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode) {
        return ResponseEntity.ok(reservationService.getAllReservationsByPassengerIdentificationNumber(identificationNumber, countryIsoCode));
    }

    @PostMapping
    public ResponseEntity<ResponsePassengerDto> addPassenger(@RequestBody @Valid AddPassengerDto addPassengerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.addPassenger(addPassengerDto));
    }

    @PatchMapping("/passport")
    public ResponseEntity<ResponsePassengerDto> patchPassengerPassportNumber(@RequestParam @NotBlank String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode,
                                                                             @RequestParam @NotBlank String newPassportNumber) {
        return ResponseEntity.ok(passengerService.updatePassengerPassport(identificationNumber, countryIsoCode, newPassportNumber));
    }

}
