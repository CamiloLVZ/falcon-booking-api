package com.falcon.booking.persistence.entity;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.exception.InvalidBoardingPassengerReservationException;
import com.falcon.booking.feature.checkIn.exception.InvalidCheckInPassengerReservationStatusException;
import com.falcon.booking.feature.flight.exception.OutOfFlightBoardingTimeException;
import com.falcon.booking.feature.flight.exception.OutOfFlightCheckInTimeException;
import com.falcon.booking.feature.reservation.exception.ReservationInvalidStatusChangeException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "passenger_reservation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_passenger_reservation_passenger_reservation", columnNames = {"id_passenger", "id_reservation"})
})

@NoArgsConstructor
@Getter
public class PassengerReservationEntity {

    public PassengerReservationEntity(PassengerEntity passenger, ReservationEntity reservation, Integer seatNumber, SeatClass seatClass) {
        this.passenger = passenger;
        this.reservation = reservation;
        this.flight = reservation.getFlight();
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
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

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassengerReservationStatus status;

    @Setter
    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    public void cancel(){
        if(this.isCanceled()) return;

        if(!(this.isReserved() || this.isCheckedIn())){
            throw new ReservationInvalidStatusChangeException(this.status, PassengerReservationStatus.CANCELED);
        }
        this.status = PassengerReservationStatus.CANCELED;
    }

    public void checkIn(int seatNumber) {
        if(!this.isReserved()){
            throw new InvalidCheckInPassengerReservationStatusException(this.status);
        }
        if(!this.flight.isCheckInAvailable()){
            throw new OutOfFlightCheckInTimeException(this.flight.getId());
        }
            this.seatNumber = seatNumber;
            this.status = PassengerReservationStatus.CHECKED_IN;
    }

    public void board(){
        if(!this.isCheckedIn() || this.isBoarded()){
            throw new InvalidBoardingPassengerReservationException(this.status);
        }
        if(!this.flight.isInBoarding()){
            throw new OutOfFlightBoardingTimeException(this.flight.getId());
        }
        this.status = PassengerReservationStatus.BOARDED;
    }

    public void expire(){
        if(this.isExpired()) return;
        if(this.isBoarded() || this.isCanceled()){
            throw new ReservationInvalidStatusChangeException(this.status, PassengerReservationStatus.EXPIRED);
        }
        this.status = PassengerReservationStatus.EXPIRED;
    }

    public boolean isReserved(){
        if (this.status==null) return false;
        return this.status.equals(PassengerReservationStatus.RESERVED);
    }
    public boolean isCheckedIn(){
        if (this.status==null) return false;
        return this.status.equals(PassengerReservationStatus.CHECKED_IN);
    }
    public boolean isCanceled(){
        if (this.status==null) return false;
        return this.status.equals(PassengerReservationStatus.CANCELED);
    }
    public boolean isBoarded(){
        if (this.status==null) return false;
        return this.status.equals(PassengerReservationStatus.BOARDED);
    }
    public boolean isExpired(){
        if (this.status==null) return false;
        return this.status.equals(PassengerReservationStatus.EXPIRED);
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
