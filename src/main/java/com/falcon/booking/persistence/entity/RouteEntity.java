package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Route.RouteInvalidStatusChangeException;
import com.falcon.booking.domain.exception.Route.RouteNotActivableException;
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

    public void activate(){
        if(this.isActive())return;
        checkIsActivable();
        this.status = RouteStatus.ACTIVE;
    }

    private void checkIsActivable() {
        List<String> errors = new ArrayList<>();
        if (airportOrigin != null && airportDestination != null) {
            if (airportOrigin.getId().equals(airportDestination.getId())) {
                errors.add("Same airport origin and destination");
            }
        } else {
            errors.add("Route must have origin and destination defined");
        }

        if (routeDays == null || routeDays.isEmpty()) {
            errors.add("Route must have at least one operating weekday");
        }

        if (routeSchedules == null || routeSchedules.isEmpty()) {
            errors.add("Route must have at least one operating schedule");
        }

        if (lengthMinutes == null || lengthMinutes <= 0) {
            errors.add("Route duration must be greater than 0");
        }

        if (defaultAirplaneType == null) {
            errors.add("Route must have default airplane type");
        }

        if (!errors.isEmpty()) {
            throw new RouteNotActivableException( String.join(", ", errors));
        }
    }

    public void deactivate(){
        if(this.isInactive())return;

        if(!this.isActive())
            throw new RouteInvalidStatusChangeException(this.status, RouteStatus.DRAFT);

        this.status = RouteStatus.INACTIVE;
    }

    public void markAsDraft(){
        if(this.isDraft())return;

        if(this.status!=null)
            throw new RouteInvalidStatusChangeException(this.status, RouteStatus.DRAFT);

        this.status = RouteStatus.DRAFT;
    }

    public boolean isActive(){
        return this.status.equals(RouteStatus.ACTIVE);
    }

    public boolean isInactive(){
        return this.status.equals(RouteStatus.INACTIVE);
    }

    public boolean isDraft(){
        return this.status.equals(RouteStatus.DRAFT);
    }

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
