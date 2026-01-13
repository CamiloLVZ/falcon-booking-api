package com.falcon.booking.domain.valueobject;

import java.time.DayOfWeek;
public enum WeekDay {

    MONDAY(DayOfWeek.MONDAY),
    TUESDAY(DayOfWeek.TUESDAY),
    WEDNESDAY(DayOfWeek.WEDNESDAY),
    THURSDAY(DayOfWeek.THURSDAY),
    FRIDAY(DayOfWeek.FRIDAY),
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY);

    private final DayOfWeek javaDay;

    WeekDay(DayOfWeek javaDay) {
        this.javaDay = javaDay;
    }

    public int getIndex() {
        return javaDay.getValue(); // 1–7
    }

    public DayOfWeek toJavaDay() {
        return javaDay;
    }
}
