package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.feature.reservation.exception.InvalidReservationAccessException;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ReservationAccessService {

    private final ReservationRepository reservationRepository;

    public ReservationAccessService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationEntity getReservationByNumberAndContactEmail(String reservationNumber, String contactEmail) {
        return reservationRepository.findByNumberAndContactEmail(
                        reservationNumber.trim().toUpperCase(Locale.ROOT),
                        contactEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(InvalidReservationAccessException::new);
    }
}
