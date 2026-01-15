package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirplaneTypeRepository extends JpaRepository<AirplaneTypeEntity, Long>, JpaSpecificationExecutor<AirplaneTypeEntity> {

    Optional<AirplaneTypeEntity> findByProducerIgnoreCaseAndModelContainingIgnoreCase(String producer, String model);
    List<AirplaneTypeEntity> findAllByStatus(AirplaneTypeStatus status);
    boolean existsByProducerAndModel(String producer, String model);
    List<AirplaneTypeEntity> findAllByProducerIgnoreCase(String producer);
    List<AirplaneTypeEntity> findAllByProducerIgnoreCaseAndStatus(String producer, AirplaneTypeStatus status);
}
