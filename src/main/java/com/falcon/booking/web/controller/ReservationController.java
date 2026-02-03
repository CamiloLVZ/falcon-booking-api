package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.web.dto.reservation.AddReservationDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
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
@RequestMapping("/reservations")
@Validated
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{reservationNumber}")
    public ResponseEntity<ResponseReservationDto> getReservation(@PathVariable String reservationNumber) {
        return ResponseEntity.ok(reservationService.getReservationByNumber(reservationNumber));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<ResponseReservationDto>> getAllReservationsByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(reservationService.getAllReservationsByFlight(flightId));
    }

    @PatchMapping("/{reservationNumber}/cancel")
    public ResponseEntity<ResponseReservationDto> cancelReservation(@PathVariable String reservationNumber) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationNumber));
    }

    @PatchMapping("/{reservationNumber}/cancel/passenger")
    public ResponseEntity<ResponseReservationDto> cancelPassengerReservation(@PathVariable String reservationNumber,
                                                                             @RequestParam @NotBlank String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode) {
        return ResponseEntity.ok(reservationService.cancelPassengerReservationByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

    @PatchMapping("/{reservationNumber}/cancel/passenger/{passportNumber}")
    public ResponseEntity<ResponseReservationDto> cancelPassengerReservation(@PathVariable String reservationNumber, @PathVariable String passportNumber) {
        return ResponseEntity.ok(reservationService.cancelPassengerReservationByPassportNumber(reservationNumber, passportNumber));
    }

    @PostMapping
    public ResponseEntity<ResponseReservationDto> addReservation(@RequestBody @Valid AddReservationDto addReservationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.addReservation(addReservationDto));
    }

    @PatchMapping("/{reservationNumber}/check-in")
    public ResponseEntity<ResponsePassengerReservationDto> checkInPassenger(@PathVariable String reservationNumber,
                                                                            @RequestParam @NotBlank String identificationNumber,
                                                                            @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode){
        return ResponseEntity.ok(reservationService.checkInByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

    @PatchMapping("/{reservationNumber}/board")
    public ResponseEntity<ResponsePassengerReservationDto> boardPassenger(@PathVariable String reservationNumber,
                                                                            @RequestParam @NotBlank String identificationNumber,
                                                                            @RequestParam @NotBlank @Size(min = 2, max = 2) String countryIsoCode){
        return ResponseEntity.ok(reservationService.boardByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

}
