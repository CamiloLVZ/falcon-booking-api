package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "country")
@NoArgsConstructor
@Getter
@Setter
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(nullable = false, name = "iso_code", unique = true, columnDefinition = "bpchar", length = 2)
    String isoCode;
}
