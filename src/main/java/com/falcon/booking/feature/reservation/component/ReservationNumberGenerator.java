package com.falcon.booking.feature.reservation.component;

import com.falcon.booking.persistence.repository.ReservationRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ReservationNumberGenerator {

    private final ReservationRepository reservationRepository;

    public ReservationNumberGenerator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public String generate() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int reservationNumberLength = 6;
        StringBuilder reservationNumber;
        boolean alreadyExists;
        do {
            reservationNumber = new StringBuilder();
            for (int i = 0; i < reservationNumberLength; i++) {
                int index = ThreadLocalRandom.current().nextInt(0, alphabet.length());
                reservationNumber.append(alphabet.charAt(index));
            }
            alreadyExists = reservationRepository.existsByNumber(reservationNumber.toString());
        } while (alreadyExists);
        return reservationNumber.toString();
    }
}
