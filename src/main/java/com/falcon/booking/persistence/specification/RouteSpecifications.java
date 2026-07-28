package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.springframework.data.jpa.domain.Specification;


public class RouteSpecifications {
    private RouteSpecifications() {}

    public static Specification<RouteEntity> hasOriginIataCode(String iataCode){
        return (root, query,cb)->{
            if (iataCode==null) {
                return cb.conjunction();
            }
            else return cb.equal(root.get("airportOrigin").get("iataCode"), iataCode);
        };
    }

    public static Specification<RouteEntity> hasDestinationIataCode(String iataCode){
        return (root, query,cb)->{

            if(iataCode==null) {
                return cb.conjunction();
            }
            else return cb.equal(root.get("airportDestination").get("iataCode"), iataCode);
        };
    }

    public static Specification<RouteEntity> hasStatus(RouteStatus status){
        return (root, query, cb)->{

            if(status==null) {
                return cb.conjunction();
            }

            else return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<RouteEntity> flightNumberContains(String flightNumber) {
        return (root, query, cb) -> {
            if (flightNumber == null || flightNumber.isBlank()) return cb.conjunction();
            return cb.like(cb.upper(root.get("flightNumber")), "%" + flightNumber.trim().toUpperCase() + "%");
        };
    }

    public static Specification<RouteEntity> hasAirplaneTypeId(Long airplaneTypeId) {
        return (root, query, cb) -> {
            if (airplaneTypeId == null) return cb.conjunction();
            return cb.equal(root.get("defaultAirplaneType").get("id"), airplaneTypeId);
        };
    }

}
