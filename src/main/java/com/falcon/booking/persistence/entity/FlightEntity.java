package com.falcon.booking.persistence.entity;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.flight.exception.FlightInvalidStatusChangeException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "flight", uniqueConstraints = {
        @UniqueConstraint(name = "uk_id_route_departure_datetime", columnNames = {"id_route", "departure_datetime"})
})
@NoArgsConstructor
@Getter
@Setter
public class FlightEntity {


    public FlightEntity(RouteEntity route, AirplaneTypeEntity airplaneType, OffsetDateTime departureDateTime, FlightStatus status) {
        this.route = route;
        this.airplaneType = airplaneType;
        this.departureDateTime = departureDateTime;
        this.status = status;
    }

    @Id
    @SequenceGenerator(name = "flight_seq_gen", sequenceName = "flight_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_route", nullable = false)
    private RouteEntity route;

    @Column(name = "departure_datetime", nullable = false)
    private OffsetDateTime departureDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airplane_type", nullable = false)
    private AirplaneTypeEntity airplaneType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Column(name = "base_price_economy", nullable = false)
    private BigDecimal basePriceEconomy = BigDecimal.ZERO;

    @Column(name = "base_price_first_class", nullable = false)
    private BigDecimal basePriceFirstClass = BigDecimal.ZERO;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "flight", orphanRemoval = true, cascade = CascadeType.ALL)
    public List<PassengerReservationEntity> reservations = new ArrayList<>();

    public BigDecimal calculatePrice(SeatClass seatClass, long occupied) {
        BigDecimal base = (seatClass == SeatClass.FIRST_CLASS) ? basePriceFirstClass : basePriceEconomy;
        return base
            .multiply(proximityFactor())
            .multiply(occupancyFactor(seatClass, occupied))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal proximityFactor() {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), departureDateTime.toLocalDate());
        if (days <= 1)  return new BigDecimal("2.00");
        if (days <= 3)  return new BigDecimal("1.75");
        if (days <= 7)  return new BigDecimal("1.50");
        if (days <= 14) return new BigDecimal("1.25");
        return BigDecimal.ONE;
    }

    private BigDecimal occupancyFactor(SeatClass seatClass, long occupied) {
        int capacity = (seatClass == SeatClass.FIRST_CLASS) ? airplaneType.getFirstClassSeats() : airplaneType.getEconomySeats();
        if (capacity == 0) return BigDecimal.ONE;
        double ratio = (double) occupied / capacity;
        if (ratio >= 0.90) return new BigDecimal("1.50");
        if (ratio >= 0.75) return new BigDecimal("1.25");
        return BigDecimal.ONE;
    }

    public boolean isScheduled() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.SCHEDULED);
    }

    public boolean isCheckInAvailable() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.CHECK_IN_AVAILABLE);
    }

    public boolean isInBoarding() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.BOARDING);
    }

    public boolean isGateClosed() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.GATE_CLOSED);
    }

    public boolean isCompleted() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.COMPLETED);
    }

    public boolean isCanceled() {
        if (this.status==null) return false;
        return this.status.equals(FlightStatus.CANCELED);
    }

    public boolean canBeReserved(int hoursBeforeToCloseCheckIn) {
        if (this.status == null) return false;
        if (this.isScheduled()) return true;
        if (this.isCheckInAvailable()) {
            Instant now = Instant.now();
            return now.isBefore(departureDateTime.minusHours(hoursBeforeToCloseCheckIn).minusMinutes(10).toInstant());
        }
        return false;
    }

    public void cancel(){
        if (this.isCanceled() ) return;

        if(this.isInBoarding() || this.isCompleted())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.CANCELED);

        this.status = FlightStatus.CANCELED;
    }

    public void startCheckIn(){
        if (this.isCheckInAvailable() ) return;

        if(!this.isScheduled())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.CHECK_IN_AVAILABLE);

        this.status = FlightStatus.CHECK_IN_AVAILABLE;
    }

    public void startBoarding(){
        if (this.isInBoarding() ) return;

        if(this.isCanceled() || this.isCompleted())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.BOARDING);

        this.status = FlightStatus.BOARDING;
    }

    public void markAsGateClosed(){
        if (this.isGateClosed() ) return;

        if(this.isCanceled() || this.isCompleted())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.GATE_CLOSED);

        this.status = FlightStatus.GATE_CLOSED;
    }

    public void markAsCompleted(){
        if (this.isCompleted() ) return;

        if(this.isCanceled())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.COMPLETED);

        this.status = FlightStatus.COMPLETED;
    }

    public void correctStatusByTime(OffsetDateTime now) {

        if (isCompleted() || isCanceled()) return;

        if (now.isAfter(departureDateTime)) {
            this.status = FlightStatus.COMPLETED;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightEntity that = (FlightEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
