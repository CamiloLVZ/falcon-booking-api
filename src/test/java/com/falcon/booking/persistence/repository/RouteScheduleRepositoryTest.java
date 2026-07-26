package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteScheduleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private RouteScheduleRepository routeScheduleRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private CountryEntity createCountry(){
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setIsoCode("CO");
        countryEntity.setName("Colombia");
        return countryEntity;
    }

    private AirportEntity createAirport(String iataCode, String name, String city, CountryEntity country, String timeZone){
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setTimezone(timeZone);
        return airport;
    }

    private AirplaneTypeEntity createAirplaneType(String producer, String model){
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer(model);
        airplaneType.setModel(producer);
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        return  airplaneType;
    }

    private RouteEntity createRoute() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(()->countryRepository.save(createCountry()));

        AirportEntity origin = airportRepository.findByIataCode("BOG")
                .orElseGet(()->airportRepository.save(createAirport("BOG", "El Dorado","Bogota", country,"America/Bogota")));

        AirportEntity destination = airportRepository.findByIataCode("MDE")
                .orElseGet(()->airportRepository.save(createAirport("MDE", "Jose Maria Cordoba","Medellin", country,"America/Bogota")));

     AirplaneTypeEntity airplaneType = airplaneTypeRepository.save(createAirplaneType("AIRBUS", "A320"));

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setDurationMinutes(60);
        route.markAsDraft();
        return routeRepository.save(route);
    }

    @DisplayName("Should delete all route schedules by route")
    @Test
    void shouldDeleteAllByRoute() {
        RouteEntity route = createRoute();
        routeScheduleRepository.save(new RouteScheduleEntity(route, LocalTime.of(8, 0)));
        routeScheduleRepository.save(new RouteScheduleEntity(route, LocalTime.of(9, 0)));

        routeScheduleRepository.deleteAllByRoute(route);
        routeScheduleRepository.flush();

        assertThat(routeScheduleRepository.findAll()).isEmpty();
    }
}
