package com.falcon.booking.persistence.specification;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.RouteEntity;
import jakarta.persistence.criteria.Predicate;
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

}
