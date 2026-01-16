package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.domain.valueobject.WeekDay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.HashSet;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "route")
@NoArgsConstructor
@Getter
@Setter
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airport_origin",nullable = false)
    AirportEntity airportOrigin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airport_destination",nullable = false)
    AirportEntity airportDestination;

    @Column(name = "flight_number", nullable = false, unique = true)
    String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_default_airplane_type", nullable = false)
    AirplaneTypeEntity defaultAirplaneType;

    @Column(name = "length_minutes", nullable = false)
    Integer lengthMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    RouteStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route",cascade=CascadeType.ALL ,orphanRemoval = true)
    private List<RouteDayEntity> routeDays;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteScheduleEntity> routeSchedules;

    public void updateWeekDays(Collection<WeekDay> newWeekDays) {
        this.routeDays.clear();

        for (WeekDay weekDay : new HashSet<>(newWeekDays)) {
            this.routeDays.add(new RouteDayEntity(this, weekDay));
        }
    }

    public void updateSchedules(Collection<LocalTime> newSchedules) {
        this.routeSchedules.clear();
        for (LocalTime schedule : new HashSet<>(newSchedules)) {
            this.routeSchedules.add(new RouteScheduleEntity(this, schedule));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteEntity entity = (RouteEntity) o;
        return Objects.equals(flightNumber, entity.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(flightNumber);
    }
}
