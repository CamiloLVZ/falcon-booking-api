package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "reservation")
@NoArgsConstructor
@Getter
public class ReservationEntity {

    public ReservationEntity(String number, FlightEntity flight, String contactEmail, Instant datetimeReservation) {
        setNumber(number);
        this.flight = flight;
        setContactEmail(contactEmail);
        this.datetimeReservation = datetimeReservation;
        this.status = ReservationStatus.RESERVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_flight", nullable = false)
    private FlightEntity flight;

    @Column(name = "contact_email", nullable = false, length = 128)
    private String contactEmail;

    @Column(name = "datetime_reservation", nullable = false)
    private Instant datetimeReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;


    public void markAsReserved(){
        this.status = ReservationStatus.RESERVED;
    }

    public void markAsCanceled(){
        this.status = ReservationStatus.CANCELED;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail.trim().toLowerCase();
    }

    private void setNumber(String number) {
        this.number = number.trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationEntity that = (ReservationEntity) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }
}
