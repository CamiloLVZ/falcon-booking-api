package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "route_schedule", uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_route_schedule_route",
                    columnNames = {
                            "departure_local_time",
                            "id_route"
                    })
})
@NoArgsConstructor
@Getter
@Setter
public class RouteScheduleEntity {

    public RouteScheduleEntity(RouteEntity route, LocalTime departureLocalTime) {
        this.route = route;
        this.departureLocalTime = departureLocalTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_route", nullable = false)
    private RouteEntity route;

    @Column(name = "departure_local_time", nullable = false)
    private LocalTime departureLocalTime;
}
