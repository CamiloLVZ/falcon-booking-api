package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long>, JpaSpecificationExecutor<RouteEntity> {

    Optional<RouteEntity> findByFlightNumber(String flightNumber);
    boolean existsByFlightNumber(String flightNumber);
    List<RouteEntity> findAllByStatus(RouteStatus status);
    @Query("SELECT r.id FROM RouteEntity r WHERE r.status = :status")
    List<Long> findIdsByStatus(@Param("status") RouteStatus status);

    @EntityGraph(attributePaths = {"airportOrigin", "defaultAirplaneType", "routeDays", "routeSchedules"})
    Optional<RouteEntity> findById(Long id);

    List<RouteEntity> findAll(Specification<RouteEntity> spec, @NonNull Sort sort);
}
