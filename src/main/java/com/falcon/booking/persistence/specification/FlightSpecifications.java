package com.falcon.booking.persistence.specification;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class FlightSpecifications {

    private FlightSpecifications() {}

    public static Specification<FlightEntity> hasRoute(RouteEntity route) {
        return (root, query,cb)->{
            if(route==null) {
                return cb.conjunction();
            }
            else return cb.equal(root.get("route"), route);
        };
    }

    public static Specification<FlightEntity> hasStatus(FlightStatus status) {

        return (root, query, cb)->{
            if(status==null) {
                return cb.conjunction();
            }
            else return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<FlightEntity> hasDateStart(OffsetDateTime date) {

        return (root, query, cb)-> {
            if (date == null)
                return null;
            else return cb.greaterThanOrEqualTo(root.get("departureDateTime"), date);
        };
    }

    public static Specification<FlightEntity> hasDateEnd(OffsetDateTime date) {

        return (root, query, cb)-> {
            if (date == null)
                return null;
            else return cb.lessThanOrEqualTo(root.get("departureDateTime"), date);
        };
    }

}
