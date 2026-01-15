package com.falcon.booking.persistence.specification;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.springframework.data.jpa.domain.Specification;

public class AirplaneTypeSpecifications {
    private AirplaneTypeSpecifications() {}

    public static Specification<AirplaneTypeEntity> hasProducer(String producer) {
        return((root, query, cb)->{

            if (producer == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("producer"), producer);
        });
    }
    public static Specification<AirplaneTypeEntity> hasModel(String model) {
        return((root, query, cb)->{

            if (model == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("model"), model);
        });
    }
    public static Specification<AirplaneTypeEntity> hasStatus(AirplaneTypeStatus status) {
        return((root, query, cb)->{

            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        });
    }

}
