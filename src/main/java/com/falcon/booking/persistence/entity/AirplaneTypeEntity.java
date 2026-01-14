package com.falcon.booking.persistence.entity;

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
