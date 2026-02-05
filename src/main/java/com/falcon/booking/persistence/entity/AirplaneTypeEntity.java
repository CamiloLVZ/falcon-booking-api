package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AirplaneTypeStatus status;

    public int getTotalSeats() {
        return  this.economySeats + this.firstClassSeats;
    }

    public String getFullName() {
        return producer + " " + model;
    }

    public void activate(){
        if(this.isActive()) return;
        if(!(this.status==null || this.isInactive())){
            throw new AirplaneTypeInvalidStatusChangeException(this.status, AirplaneTypeStatus.ACTIVE);
        }

        this.status = AirplaneTypeStatus.ACTIVE;
    }

    public void deactivate(){
        if(this.isInactive()) return;
        if(!this.isActive()){
            throw new AirplaneTypeInvalidStatusChangeException(this.status, AirplaneTypeStatus.INACTIVE);
        }

        this.status = AirplaneTypeStatus.INACTIVE;
    }

    public void retire(){
        if(this.isRetired()) return;
        if(!this.isInactive()){
            throw new AirplaneTypeInvalidStatusChangeException(this.status, AirplaneTypeStatus.RETIRED);
        }

        this.status = AirplaneTypeStatus.RETIRED;
    }

    public boolean isActive(){
        if (this.status==null) return false;
        return this.status.equals(AirplaneTypeStatus.ACTIVE);
    }

    public boolean isInactive(){
        if (this.status==null) return false;
        return this.status.equals(AirplaneTypeStatus.INACTIVE);
    }

    public boolean isRetired(){
        if (this.status==null) return false;
        return this.status.equals(AirplaneTypeStatus.RETIRED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirplaneTypeEntity that = (AirplaneTypeEntity) o;
        return Objects.equals(producer, that.producer) && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, model);
    }


}
