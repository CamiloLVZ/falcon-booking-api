package com.falcon.booking.persistence.entity;

import com.falcon.booking.common.enums.BoardingPassStatus;
import com.falcon.booking.feature.boardingPass.exception.BoardingPassAlreadyBoardedException;
import com.falcon.booking.feature.boardingPass.exception.BoardingPassExpiredException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "boarding_pass")
@NoArgsConstructor
@Getter
public class BoardingPassEntity {

    public BoardingPassEntity(PassengerReservationEntity passengerReservation, UUID qrToken) {
        this.passengerReservation = passengerReservation;
        this.qrToken = qrToken;
        this.status = BoardingPassStatus.ISSUED;
        this.generatedAt = Instant.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_passenger_reservation", nullable = false,unique = true)
    private PassengerReservationEntity passengerReservation;

    @Column(name = "qr_token", nullable = false, unique = true)
    private UUID qrToken;

    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false)
    private BoardingPassStatus status;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "emailed_at")
    private Instant emailedAt;

    @Column(name = "boarded_at")
    private Instant boardedAt;

    public void markAsEmailed() {
        this.emailedAt = Instant.now();
    }

    public void markAsBoarded(){
        validateCanChangeStatus();
        this.status = BoardingPassStatus.BOARDED;
        this.boardedAt = Instant.now();

    }

    public void markAsExpired(){
       validateCanChangeStatus();
        this.status = BoardingPassStatus.EXPIRED;
    }

    private void validateCanChangeStatus() {

        if (status == BoardingPassStatus.BOARDED) {
            throw new BoardingPassAlreadyBoardedException(buildPassengerReference());
        }

        if (status == BoardingPassStatus.EXPIRED) {
            throw new BoardingPassExpiredException(buildPassengerReference());
        }

    }
    private String buildPassengerReference() {

        PassengerEntity passenger = passengerReservation.getPassenger();

        return passenger.getFullName() + ", " +passenger.getIdentification();

    }


}
