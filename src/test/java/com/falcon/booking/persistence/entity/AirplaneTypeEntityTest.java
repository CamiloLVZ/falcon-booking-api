package com.falcon.booking.persistence.entity;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.exception.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatConfigurationException;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatNumberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirplaneTypeEntityTest {

    // ─── Helpers ─────────────────────────────────────────────────────────────

    AirplaneTypeEntity createAirplaneType(String producer, String model) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setProducer(producer);
        entity.setModel(model);
        return entity;
    }

    AirplaneTypeEntity createAirplaneTypeWithStatus(AirplaneTypeStatus status) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setStatus(status);
        return entity;
    }

    /** Creates a fully configured entity (seats + columns) for tests that need seat data. */
    AirplaneTypeEntity createConfiguredAirplaneType(int economySeats, int firstClassSeats, String seatColumns) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.configureSeats(economySeats, firstClassSeats, seatColumns);
        return entity;
    }

    // ─── Status transition tests ──────────────────────────────────────────────

    @DisplayName("Should activate inactive AirplaneType")
    @Test
    void shouldActivateFromInactive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.INACTIVE);
        airplaneType.activate();
        assertThat(airplaneType.isActive()).isTrue();
    }

    @DisplayName("Should activate AirplaneType with null status")
    @Test
    void shouldActivateFromNullStatus() {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.activate();
        assertThat(airplaneType.isActive()).isTrue();
    }

    @DisplayName("Should activate already active AirplaneType (idempotent)")
    @Test
    void shouldActivateFromActive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType.activate();
        assertThat(airplaneType.isActive()).isTrue();
    }

    @DisplayName("Should throw exception when activating a retired AirplaneType")
    @Test
    void shouldThrowExceptionActivateFromRetired() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.RETIRED);

        AirplaneTypeInvalidStatusChangeException exception =
                assertThrows(AirplaneTypeInvalidStatusChangeException.class, airplaneType::activate);

        assertThat(exception.getMessage()).contains("RETIRED to ACTIVE");
    }

    @DisplayName("Should deactivate active AirplaneType")
    @Test
    void shouldDeactivateFromActive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType.deactivate();
        assertThat(airplaneType.isInactive()).isTrue();
    }

    @DisplayName("Should deactivate already inactive AirplaneType (idempotent)")
    @Test
    void shouldDeactivateFromInactive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.INACTIVE);
        airplaneType.deactivate();
        assertThat(airplaneType.isInactive()).isTrue();
    }

    @DisplayName("Should throw exception when deactivating a retired AirplaneType")
    @Test
    void shouldThrowExceptionDeactivateFromRetired() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.RETIRED);

        AirplaneTypeInvalidStatusChangeException exception =
                assertThrows(AirplaneTypeInvalidStatusChangeException.class, airplaneType::deactivate);

        assertThat(exception.getMessage()).contains("RETIRED to INACTIVE");
    }

    @DisplayName("Should retire inactive AirplaneType")
    @Test
    void shouldRetireFromInactive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.INACTIVE);
        airplaneType.retire();
        assertThat(airplaneType.isRetired()).isTrue();
    }

    @DisplayName("Should retire already retired AirplaneType (idempotent)")
    @Test
    void shouldRetireFromRetired() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.RETIRED);
        airplaneType.retire();
        assertThat(airplaneType.isRetired()).isTrue();
    }

    @DisplayName("Should throw exception when retiring an active AirplaneType")
    @Test
    void shouldThrowExceptionRetireFromActive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.ACTIVE);

        AirplaneTypeInvalidStatusChangeException exception =
                assertThrows(AirplaneTypeInvalidStatusChangeException.class, airplaneType::retire);

        assertThat(exception.getMessage()).contains("ACTIVE to RETIRED");
    }

    // ─── equals / hashCode ───────────────────────────────────────────────────

    @DisplayName("Should return true when same producer+model in equals")
    @Test
    void shouldReturnTrue_equalsSameAirplaneTypeEntities() {
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "320");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        assertThat(airplaneType1).isEqualTo(airplaneType2);
    }

    @DisplayName("Should return false when different producer+model in equals")
    @Test
    void shouldReturnFalse_differentSameAirplaneTypeEntities() {
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "330");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        assertThat(airplaneType1).isNotEqualTo(airplaneType2);
    }

    @DisplayName("Should have same hashCode for equal entities")
    @Test
    void shouldBeEqualHashCode_sameAirplaneTypeEntities() {
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "320");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        assertThat(airplaneType1.hashCode()).isEqualTo(airplaneType2.hashCode());
    }

    @DisplayName("Should have different hashCode for different entities")
    @Test
    void shouldNotEqualHashCode_differentAirplaneTypeEntities() {
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "330");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        assertThat(airplaneType1.hashCode()).isNotEqualTo(airplaneType2.hashCode());
    }

    // ─── configureSeats — happy path ─────────────────────────────────────────

    @DisplayName("Should configure seats successfully with valid data")
    @Test
    void shouldConfigureSeats_validInput() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.configureSeats(150, 12, "ABCDEF");

        assertThat(entity.getEconomySeats()).isEqualTo(150);
        assertThat(entity.getFirstClassSeats()).isEqualTo(12);
        assertThat(entity.getSeatColumns()).isEqualTo("ABCDEF");
        assertThat(entity.getTotalSeats()).isEqualTo(162);
    }

    @DisplayName("Should allow zero first-class seats")
    @Test
    void shouldConfigureSeats_zeroFirstClass() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.configureSeats(120, 0, "ABC");

        assertThat(entity.getFirstClassSeats()).isEqualTo(0);
        assertThat(entity.getTotalSeats()).isEqualTo(120);
    }

    // ─── configureSeats — validation errors ──────────────────────────────────

    @DisplayName("Should throw when economy seats is negative")
    @Test
    void shouldThrow_negativeEconomySeats() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(-1, 10, "ABC"));

        assertThat(ex.getMessage()).containsIgnoringCase("economy");
    }

    @DisplayName("Should throw when first class seats is negative")
    @Test
    void shouldThrow_negativeFirstClassSeats() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(100, -1, "ABC"));

        assertThat(ex.getMessage()).containsIgnoringCase("first class");
    }

    @DisplayName("Should throw when total seats is zero")
    @Test
    void shouldThrow_zeroTotalSeats() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(0, 0, "ABC"));

        assertThat(ex.getMessage()).containsIgnoringCase("at least one seat");
    }

    @DisplayName("Should throw when seatColumns is null")
    @Test
    void shouldThrow_nullSeatColumns() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(100, 10, null));

        assertThat(ex.getMessage()).containsIgnoringCase("cannot be empty");
    }

    @DisplayName("Should throw when seatColumns is blank")
    @Test
    void shouldThrow_blankSeatColumns() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(100, 10, "   "));

        assertThat(ex.getMessage()).containsIgnoringCase("cannot be empty");
    }

    @DisplayName("Should throw when seatColumns contains lowercase letters")
    @Test
    void shouldThrow_lowercaseSeatColumns() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(100, 10, "abcdef"));

        assertThat(ex.getMessage()).containsIgnoringCase("uppercase");
    }

    @DisplayName("Should throw when seatColumns contains duplicate letters")
    @Test
    void shouldThrow_duplicateSeatColumns() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();

        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(100, 10, "AABCDE"));

        assertThat(ex.getMessage()).containsIgnoringCase("duplicate");
    }

    @DisplayName("Should throw when total seats is not a multiple of column count")
    @Test
    void shouldThrow_seatsNotMultipleOfColumns() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        // 11 seats, 6 columns → 11 % 6 != 0
        InvalidSeatConfigurationException ex = assertThrows(
                InvalidSeatConfigurationException.class,
                () -> entity.configureSeats(10, 1, "ABCDEF"));

        assertThat(ex.getMessage()).containsIgnoringCase("multiple");
    }

    // ─── getSeatLabel ─────────────────────────────────────────────────────────

    @DisplayName("Should return correct seat label for first seat")
    @Test
    void shouldReturnSeatLabel_firstSeat() {
        // 12 seats, 6 columns (A-F) → seat 1 = row 1, col A = "1A"
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");
        assertThat(entity.getSeatLabel(1)).isEqualTo("1A");
    }

    @DisplayName("Should return correct seat label for last seat in first row")
    @Test
    void shouldReturnSeatLabel_lastInFirstRow() {
        // 12 seats, 6 columns → seat 6 = row 1, col F = "1F"
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");
        assertThat(entity.getSeatLabel(6)).isEqualTo("1F");
    }

    @DisplayName("Should return correct seat label for first seat in second row")
    @Test
    void shouldReturnSeatLabel_firstInSecondRow() {
        // seat 7 = row 2, col A = "2A"
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");
        assertThat(entity.getSeatLabel(7)).isEqualTo("2A");
    }

    @DisplayName("Should return correct seat label for last seat")
    @Test
    void shouldReturnSeatLabel_lastSeat() {
        // 12 seats, 6 columns → seat 12 = row 2, col F = "2F"
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");
        assertThat(entity.getSeatLabel(12)).isEqualTo("2F");
    }

    @DisplayName("Should throw when seat number is zero")
    @Test
    void shouldThrow_seatNumberZero() {
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");

        assertThrows(InvalidSeatNumberException.class, () -> entity.getSeatLabel(0));
    }

    @DisplayName("Should throw when seat number exceeds total seats")
    @Test
    void shouldThrow_seatNumberExceedsTotal() {
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");

        assertThrows(InvalidSeatNumberException.class, () -> entity.getSeatLabel(13));
    }

    @DisplayName("Should throw with message containing valid range")
    @Test
    void shouldThrow_seatNumberMessageContainsRange() {
        AirplaneTypeEntity entity = createConfiguredAirplaneType(6, 6, "ABCDEF");

        InvalidSeatNumberException ex = assertThrows(
                InvalidSeatNumberException.class,
                () -> entity.getSeatLabel(99));

        assertThat(ex.getMessage()).contains("1-12");
    }
}
