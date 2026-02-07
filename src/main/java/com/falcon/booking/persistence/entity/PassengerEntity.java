package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.valueobject.PassengerGender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "passenger", uniqueConstraints = {
        @UniqueConstraint(name = "uk_identification_number_id_country_nationality",
                columnNames = {"identification_number", "id_country_nationality"})
})
@NoArgsConstructor
@Getter
@Setter
public class PassengerEntity {

    public PassengerEntity(String firstName, String lastName, PassengerGender gender, LocalDate dateOfBirth, String passportNumber, String identificationNumber) {
         setFirstName(firstName);
         setLastName(lastName);
         setGender(gender);
         setDateOfBirth(dateOfBirth);
         setPassportNumber(passportNumber);
         setIdentificationNumber(identificationNumber);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    PassengerGender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_country_nationality", nullable = false)
    private CountryEntity countryNationality;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "passport_number", unique = true)
    private String passportNumber;

    @Column(name = "identification_number", length = 20, nullable = false)
    private String identificationNumber;


    public void setFirstName(String firstName) {
        this.firstName = StringNormalizer.normalize(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = StringNormalizer.normalize(lastName);
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = StringNormalizer.normalize(passportNumber);
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = StringNormalizer.normalize(identificationNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassengerEntity that = (PassengerEntity) o;
        return Objects.equals(countryNationality, that.countryNationality) && Objects.equals(identificationNumber, that.identificationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryNationality, identificationNumber);
    }
}
