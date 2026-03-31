package com.falcon.booking.domain.service;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightBatchPersistenceService {

    @PersistenceContext
    private EntityManager entityManager;
    private final FlightRepository flightRepository;

    public FlightBatchPersistenceService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveBatch(List<FlightEntity> batch){
        flightRepository.saveAll(batch);
        entityManager.flush();
        entityManager.clear();
    }
}
