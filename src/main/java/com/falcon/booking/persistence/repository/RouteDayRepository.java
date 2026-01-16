package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RouteDayEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteDayRepository extends JpaRepository<RouteDayEntity, Long> {

    @Modifying
    @Query("DELETE FROM RouteDayEntity rd WHERE rd.route = :route")
    void deleteAllByRoute(@Param("route") RouteEntity route);
}
