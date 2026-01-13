package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.domain.service.CountryService;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryService countryService;
    private final AirportService airportService ;

    @Autowired
    public CountryController(CountryService countryService, AirportService airportService) {
        this.countryService = countryService;
        this.airportService = airportService;
    }

    @GetMapping("/{isoCode}")
    public ResponseEntity<CountryDto> getCountry(@PathVariable String isoCode) {
        CountryDto country = countryService.getCountryByIsoCode(isoCode);
        return ResponseEntity.ok(country);
    }

    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<CountryDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{isoCode}/airports")
    public ResponseEntity<List<AirportDto>> getAirportsByCountryIsoCode(@PathVariable String isoCode) {

        return ResponseEntity.ok(airportService.getAirportsByCountryIsoCode(isoCode));
    }

}
