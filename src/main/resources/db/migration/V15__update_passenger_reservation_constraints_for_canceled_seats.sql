ALTER TABLE passenger_reservation
DROP CONSTRAINT uk_passenger_reservation_flight_seat_number;

-- Seat unique in flight only for active
CREATE UNIQUE INDEX ux_seat_flight_active
    ON passenger_reservation (seat_number, id_flight)
    WHERE status <> 'CANCELED';
