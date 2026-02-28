package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AirplaneTypeRepository extends JpaRepository<AirplaneTypeEntity, Long>, JpaSpecificationExecutor<AirplaneTypeEntity> {
    boolean existsByProducerAndModel(String producer, String model);
}
