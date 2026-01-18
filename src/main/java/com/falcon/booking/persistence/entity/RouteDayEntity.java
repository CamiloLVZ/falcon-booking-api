package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Entity
@Table(name = "route_day", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_route_day_route_week_day",
                columnNames = {"id_route", "week_day"}
        )
})
@NoArgsConstructor
@Getter
@Setter
public class RouteDayEntity {

    public RouteDayEntity(RouteEntity route, DayOfWeek weekDay) {
        this.route = route;
        this.weekDay = weekDay;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_route", nullable=false)
    private RouteEntity route;

    @Enumerated(EnumType.STRING)
    @Column(name="week_day",nullable = false, length = 10)
    private DayOfWeek weekDay;

}
