package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "airport")
@NoArgsConstructor
@Getter
@Setter
public class AirportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3, unique = true)
    private String iataCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_country", nullable = false)
    private CountryEntity country;

    @Column(nullable = false, length = 20)
    private String timezone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirportEntity that = (AirportEntity) o;
        return Objects.equals(iataCode, that.iataCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(iataCode);
    }
}
