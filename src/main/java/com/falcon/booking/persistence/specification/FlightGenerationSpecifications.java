package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import org.springframework.data.jpa.domain.Specification;

public class FlightGenerationSpecifications {
    private FlightGenerationSpecifications() {}

    public static Specification<FlightGenerationEntity> hasType(FlightGenerationType type) {
        return (root, query, cb) -> {
            if (type == null) return cb.conjunction();
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<FlightGenerationEntity> hasStatus(FlightGenerationStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<FlightGenerationEntity> hasRouteId(Long routeId) {
        return (root, query, cb) -> {
            if (routeId == null) return cb.conjunction();
            return cb.equal(root.get("idRoute"), routeId);
        };
    }
}