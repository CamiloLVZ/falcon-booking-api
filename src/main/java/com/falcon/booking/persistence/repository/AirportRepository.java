package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<AirportEntity, Long> {

    Optional<AirportEntity> findByIataCode(String iataCode);
    List<AirportEntity> findAllByCountry(CountryEntity country);
}
