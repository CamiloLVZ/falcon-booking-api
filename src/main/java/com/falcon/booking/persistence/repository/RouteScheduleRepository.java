package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.entity.RouteScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteScheduleRepository extends JpaRepository<RouteScheduleEntity, Long> {

    @Modifying
    @Query("DELETE FROM RouteScheduleEntity rs WHERE rs.route = :route")
    void deleteAllByRoute(@Param("route") RouteEntity route);
}
