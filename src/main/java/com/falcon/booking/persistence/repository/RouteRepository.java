package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long>, JpaSpecificationExecutor<RouteEntity> {

    Optional<RouteEntity> findByFlightNumber(String flightNumber);
    boolean existsByFlightNumber(String flightNumber);
    List<RouteEntity> findAllByStatus(RouteStatus status);
}
