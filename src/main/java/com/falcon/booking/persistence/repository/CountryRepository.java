package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {
    Optional<CountryEntity> findByIsoCode(String isoCode);
}
