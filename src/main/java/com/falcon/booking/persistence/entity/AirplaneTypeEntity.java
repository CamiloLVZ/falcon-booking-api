package com.falcon.booking.persistence.entity;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.airplaneType.dto.SeatDefinition;
import com.falcon.booking.feature.airplaneType.exception.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatConfigurationException;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatNumberException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Entity
@Table(name = "airplane_type", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_airplane_type_producer_model",
                columnNames = {"producer", "model"}

        )})
@NoArgsConstructor
@Getter
@Setter
public class AirplaneTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String producer;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer economySeats;

    @Column(name = "first_class_seats", nullable = false)
    private Integer firstClassSeats;

    @Column(name = "seat_columns", nullable = false, length = 10)
    private String seatColumns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AirplaneTypeStatus status;

    public int getTotalSeats() {
        return economySeats + firstClassSeats;
    }

    public String getFullName() {
        return producer + " " + model;
    }

    public void activate() {
        if (isActive()) return;

        if (!(status == null || isInactive())) {
            throw new AirplaneTypeInvalidStatusChangeException(
                    status,
                    AirplaneTypeStatus.ACTIVE
            );
        }

        status = AirplaneTypeStatus.ACTIVE;
    }

    public void deactivate() {
        if (isInactive()) return;

        if (!isActive()) {
            throw new AirplaneTypeInvalidStatusChangeException(
                    status,
                    AirplaneTypeStatus.INACTIVE
            );
        }

        status = AirplaneTypeStatus.INACTIVE;
    }

    public void retire() {
        if (isRetired()) return;

        if (!isInactive()) {
            throw new AirplaneTypeInvalidStatusChangeException(
                    status,
                    AirplaneTypeStatus.RETIRED
            );
        }

        status = AirplaneTypeStatus.RETIRED;
    }

    public boolean isActive() {
        return status == AirplaneTypeStatus.ACTIVE;
    }

    public boolean isInactive() {
        return status == AirplaneTypeStatus.INACTIVE;
    }

    public boolean isRetired() {
        return status == AirplaneTypeStatus.RETIRED;
    }

    public void configureSeats(int economySeats, int firstClassSeats, String seatColumns) {

        if (economySeats < 0) {
            throw new InvalidSeatConfigurationException("The number of economy seats cannot be negative.");
        }

        if (firstClassSeats < 0) {
            throw new InvalidSeatConfigurationException("The number of first class seats cannot be negative.");
        }

        int totalSeats = economySeats + firstClassSeats;

        if (totalSeats == 0) {
            throw new InvalidSeatConfigurationException("The airplane must have at least one seat.");
        }

        validateSeatColumns(seatColumns);

        if (totalSeats % seatColumns.length() != 0) {
            throw new InvalidSeatConfigurationException("The total number of seats (%d) must be a multiple of the number of seat columns (%d).".formatted(totalSeats, seatColumns.length()));
        }

        this.economySeats = economySeats;
        this.firstClassSeats = firstClassSeats;
        this.seatColumns = seatColumns;
    }

    private void validateSeatColumns(String seatColumns) {

        if (seatColumns == null || seatColumns.isBlank()) {
            throw new InvalidSeatConfigurationException("Seat columns cannot be empty.");
        }

        if (!seatColumns.matches("[A-Z]+")) {
            throw new InvalidSeatConfigurationException("Seat columns must contain only uppercase letters.");
        }

        long distinctLetters = seatColumns.chars().distinct().count();

        if (distinctLetters != seatColumns.length()) {
            throw new InvalidSeatConfigurationException("Seat columns cannot contain duplicated letters.");
        }
    }

    public SeatClass getSeatClass(int seatNumber) {
        validateSeatNumber(seatNumber);
        return seatNumber <= firstClassSeats ? SeatClass.FIRST_CLASS : SeatClass.ECONOMY;
    }

    public String getSeatLabel(int seatNumber) {

        validateSeatNumber(seatNumber);
        int seatsPerRow = seatColumns.length();
        int row = ((seatNumber - 1) / seatsPerRow) + 1;
        char column = seatColumns.charAt((seatNumber - 1) % seatsPerRow);

        return row + String.valueOf(column);
    }

    private void validateSeatNumber(int seatNumber) {
        if (seatNumber < 1 || seatNumber > getTotalSeats()) {
            throw new InvalidSeatNumberException(seatNumber, getTotalSeats());
        }
    }

    public int getRowCount(SeatClass seatClass) {

        int seats = switch (seatClass) {
            case FIRST_CLASS -> firstClassSeats;
            case ECONOMY -> economySeats;
        };
        return (int) Math.ceil(seats / (double) seatColumns.length());
    }

    public int getFirstRow(SeatClass seatClass) {
        return switch (seatClass) {
            case FIRST_CLASS -> 1;
            case ECONOMY -> getRowCount(SeatClass.FIRST_CLASS) + 1;
        };
    }

    public int getLastRow(SeatClass seatClass) {
        return switch (seatClass) {
            case FIRST_CLASS -> getRowCount(SeatClass.FIRST_CLASS);
            case ECONOMY -> getRowCount(SeatClass.FIRST_CLASS) + getRowCount(SeatClass.ECONOMY);
        };
    }

    public List<SeatDefinition> getSeats() {

        return IntStream.rangeClosed(1, getTotalSeats())
                .mapToObj(seat -> new SeatDefinition(
                        seat,
                        getSeatLabel(seat),
                        getSeatClass(seat)
                ))
                .toList();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AirplaneTypeEntity that = (AirplaneTypeEntity) o;

        return Objects.equals(producer, that.producer)
                && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, model);
    }
}