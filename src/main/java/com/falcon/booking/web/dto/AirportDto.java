package com.falcon.booking.web.dto;

public record AirportDto(String iataCode, String name, String city, CountryDto country, String timezone) { }
