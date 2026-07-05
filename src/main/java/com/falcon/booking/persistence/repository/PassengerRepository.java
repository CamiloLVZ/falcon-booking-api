package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {
    boolean existsByPassportNumber(String passportNumber);
    Optional<PassengerEntity> findByPassportNumber(String passportNumber);
    Optional<PassengerEntity> findByIdentificationNumberAndCountryNationality(String identificationNumber, CountryEntity countryNationality);

    Page<PassengerEntity> findDistinctByPassengerReservationsFlightId(Long flightId, Pageable pageable);
}


