package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Reservation.PassengerNotFoundInReservationException;
import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import com.falcon.booking.domain.valueobject.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PassengerReservationEntity> passengerReservations;

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail.trim().toLowerCase();
    }

    private void setNumber(String number) {
        this.number = number.trim().toUpperCase();
    }

    public void cancel(){
        this.status = ReservationStatus.CANCELED;
        for (PassengerReservationEntity passengerReservation : this.passengerReservations) {
            passengerReservation.cancel();
        }
    }

    public void cancelPassenger(PassengerEntity passenger){
        boolean passengerFound = false;
        for (PassengerReservationEntity passengerReservation : this.passengerReservations) {
            if(passengerReservation.getPassenger().equals(passenger)){
                passengerFound = true;
                passengerReservation.cancel();
            }
        }
        if(this.allPassengerCanceled()){
            this.status = ReservationStatus.CANCELED;
        }

        if(!passengerFound)
            throw new PassengerNotFoundInReservationException(passenger.getIdentificationNumber(),passenger.getCountryNationality().getIsoCode() , this.number );
    }

    public boolean allPassengerCanceled(){
        for (PassengerReservationEntity passengerReservation : this.passengerReservations) {
            if(!passengerReservation.getStatus().equals(PassengerReservationStatus.CANCELED)){
                return false;
            }
        }
        return true;
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
