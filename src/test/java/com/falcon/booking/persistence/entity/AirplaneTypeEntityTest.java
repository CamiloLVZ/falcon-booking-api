package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirplaneTypeEntityTest {

    AirplaneTypeEntity createAirplaneType(String producer, String model){
        AirplaneTypeEntity airplaneTypeEntity = new AirplaneTypeEntity();
        airplaneTypeEntity.setProducer(producer);
        airplaneTypeEntity.setProducer(model);
        return airplaneTypeEntity;
    }

    AirplaneTypeEntity createAirplaneTypeWithStatus(AirplaneTypeStatus status){
        AirplaneTypeEntity airplaneTypeEntity = new AirplaneTypeEntity();
        airplaneTypeEntity.setStatus(status);
        return airplaneTypeEntity;
    }

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

    @DisplayName("Should activate already active AirplaneType")
    @Test
    void shouldActivateFromActive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.ACTIVE);

        airplaneType.activate();

        assertThat(airplaneType.isActive()).isTrue();
    }

    @DisplayName("Should throw exception at activate retired AirplaneType")
    @Test
    void shouldThrowExceptionActivateFromRetired(){
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

    @DisplayName("Should deactivate already inactive AirplaneType")
    @Test
    void shouldDeactivateFromInactive() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.INACTIVE);

        airplaneType.deactivate();

        assertThat(airplaneType.isInactive()).isTrue();
    }

    @DisplayName("Should throw exception at deactivate retired AirplaneType")
    @Test
    void shouldThrowExceptionDeactivateFromRetired(){
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

    @DisplayName("Should retire already retired AirplaneType")
    @Test
    void shouldRetireFromRetired() {
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.RETIRED);

        airplaneType.retire();

        assertThat(airplaneType.isRetired()).isTrue();
    }

    @DisplayName("Should throw exception at retire active AirplaneType")
    @Test
    void shouldThrowExceptionRetireFromActive(){
        AirplaneTypeEntity airplaneType = createAirplaneTypeWithStatus(AirplaneTypeStatus.ACTIVE);

        AirplaneTypeInvalidStatusChangeException exception =
                assertThrows(AirplaneTypeInvalidStatusChangeException.class, airplaneType::retire);

        assertThat(exception.getMessage()).contains("ACTIVE to RETIRED");
    }

    @DisplayName("Should return true when same airplaneTypeEntity in equals")
    @Test
    void shouldReturnTrue_equalsSameAirplaneTypeEntities(){
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "320");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        boolean isEqual = airplaneType1.equals(airplaneType2);

        assertThat(isEqual).isTrue();
    }

    @DisplayName("Should return false when different airplaneTypeEntity in equals")
    @Test
    void shouldReturnFalse_differentSameAirplaneTypeEntities(){
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "330");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        boolean isEqual = airplaneType1.equals(airplaneType2);

        assertThat(isEqual).isFalse();
    }

    @DisplayName("Should have same hash code two equal airplaneTypeEntities")
    @Test
    void shouldBeEqualHashCode_sameAirplaneTypeEntities(){
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "320");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        boolean sameHashCode = airplaneType1.hashCode()==airplaneType2.hashCode();

        assertThat(sameHashCode).isTrue();
    }

    @DisplayName("Should have different hash code two different airplaneTypeEntities")
    @Test
    void shouldNotEqualHashCode_differentAirplaneTypeEntities(){
        AirplaneTypeEntity airplaneType1 = createAirplaneType("Airbus", "330");
        AirplaneTypeEntity airplaneType2 = createAirplaneType("Airbus", "320");

        boolean sameHashCode = airplaneType1.hashCode()==airplaneType2.hashCode();

        assertThat(sameHashCode).isFalse();
    }

}
