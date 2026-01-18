package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.RouteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

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

    public void updateWeekDays(Collection<DayOfWeek> newWeekDays) {
        this.routeDays.clear();

        for (DayOfWeek weekDay : new HashSet<>(newWeekDays)) {
            this.routeDays.add(new RouteDayEntity(this, weekDay));
        }
    }

    public void updateSchedules(Collection<LocalTime> newSchedules) {
        this.routeSchedules.clear();
        for (LocalTime schedule : new HashSet<>(newSchedules)) {
            this.routeSchedules.add(new RouteScheduleEntity(this, schedule));
        }
    }

    public Set<DayOfWeek> getOperatingDays(){
        Set<DayOfWeek> weekDays = new HashSet<>();
        for(RouteDayEntity routeDay : routeDays){
            weekDays.add(routeDay.getWeekDay());
        }
        return weekDays;
    }

    public Set<LocalTime> getOperatingSchedules(){
        Set<LocalTime> schedules = new HashSet<>();
        for(RouteScheduleEntity routeSchedule : routeSchedules){
            schedules.add(routeSchedule.getDepartureLocalTime());
        }
        return schedules;
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
