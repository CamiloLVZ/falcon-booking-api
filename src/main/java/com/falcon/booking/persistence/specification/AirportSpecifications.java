package com.falcon.booking.persistence.specification;

import com.falcon.booking.persistence.entity.AirportEntity;
import org.springframework.data.jpa.domain.Specification;

public class AirportSpecifications {
    private AirportSpecifications() {}

    public static Specification<AirportEntity> hasCountryIsoCode(String isoCode) {
        return (root, query, cb) -> {
            if (isoCode == null || isoCode.isBlank()) return cb.conjunction();
            return cb.equal(root.get("country").get("isoCode"), isoCode.toUpperCase().trim());
        };
    }

    public static Specification<AirportEntity> nameOrIataContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("iataCode")), pattern)
            );
        };
    }
}