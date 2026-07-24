package com.falcon.booking.security.evaluator;

import com.falcon.booking.persistence.entity.ReservationEntity;

import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.security.jwt.JwtPayload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReservationSecurityEvaluator {

    private final ReservationRepository reservationRepository;

    public ReservationSecurityEvaluator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean isReservationOwnerOrAdmin(String reservationNumber, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof JwtPayload payload)) {
            return false;
        }

        if (payload.roles().contains("ADMIN")) {
            return true;
        }

        Optional<ReservationEntity> reservationOpt = reservationRepository.findByNumber(reservationNumber);
        if (reservationOpt.isEmpty()) {
            return true;
        }

        ReservationEntity reservation = reservationOpt.get();
        return reservation.getUser() != null && reservation.getUser().getId().equals(payload.userId());
    }
}
