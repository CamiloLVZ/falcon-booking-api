package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Reservation.ReservationInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "passenger_reservation", uniqueConstraints = {
        @UniqueConstraint(name="uk_passenger_reservation_passenger_reservation", columnNames = {"id_passenger", "id_reservation"}),
        @UniqueConstraint(name = "uk_passenger_reservation_flight_seat_number", columnNames = {"id_flight", "seat_number"})
})
@NoArgsConstructor
@Getter
public class PassengerReservationEntity {

    public PassengerReservationEntity(PassengerEntity passenger, ReservationEntity reservation, Integer seatNumber) {
        this.passenger = passenger;
        this.reservation = reservation;
        this.flight = reservation.getFlight();
        this.seatNumber = seatNumber;
        this.status = PassengerReservationStatus.RESERVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_passenger", nullable = false)
    private PassengerEntity passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reservation", nullable = false)
    private ReservationEntity reservation;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_flight", nullable = false)
    private FlightEntity flight;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassengerReservationStatus status;

    public void reassignSeat(Integer seatNumber){
        this.seatNumber = seatNumber;
    }

    public void markAsCheckedIn(){
        if(this.status == PassengerReservationStatus.CHECKED_IN) return;

        if(!this.status.equals(PassengerReservationStatus.RESERVED)){
            throw new ReservationInvalidStatusChangeException(this.status, PassengerReservationStatus.CHECKED_IN);
        }
        this.status = PassengerReservationStatus.CHECKED_IN;
    }

    public void markAsCanceled(){
        if(this.status == PassengerReservationStatus.CANCELED) return;

        if(!(this.status.equals(PassengerReservationStatus.RESERVED) || this.status.equals(PassengerReservationStatus.CHECKED_IN))){
            throw new ReservationInvalidStatusChangeException(this.status, PassengerReservationStatus.CANCELED);
        }
        this.status = PassengerReservationStatus.CANCELED;
    }

    public void markAsBoarded(){
        if(this.status == PassengerReservationStatus.BOARDED) return;

        if(!this.status.equals(PassengerReservationStatus.CHECKED_IN)){
            throw new ReservationInvalidStatusChangeException(this.status, PassengerReservationStatus.BOARDED);
        }
        this.status = PassengerReservationStatus.BOARDED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassengerReservationEntity that = (PassengerReservationEntity) o;
        return Objects.equals(passenger, that.passenger) && Objects.equals(reservation, that.reservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passenger, reservation);
    }
}
