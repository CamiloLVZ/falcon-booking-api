CREATE TABLE reservation(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    number VARCHAR(12) NOT NULL UNIQUE,
    id_flight BIGINT NOT NULL,
    contact_email VARCHAR(128) NOT NULL,
    datetime_reservation TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_reservation PRIMARY KEY (id),
    CONSTRAINT fk_reservation_flight FOREIGN KEY (id_flight) REFERENCES flight (id),
    CONSTRAINT chk_reservation_status CHECK(status IN ('RESERVED','CANCELED'))
);
CREATE INDEX idx_reservation_id_flight ON reservation(id_flight);

CREATE TABLE passenger_reservation(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    id_passenger BIGINT NOT NULL,
    id_reservation BIGINT NOT NULL,
    id_flight BIGINT NOT NULL,
    seat_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_passenger_reservation PRIMARY KEY (id),
    CONSTRAINT fk_passenger_reservation_passenger FOREIGN KEY (id_passenger) REFERENCES passenger(id),
    CONSTRAINT fk_passenger_reservation_reservation FOREIGN KEY (id_reservation) REFERENCES reservation(id),
    CONSTRAINT fk_passenger_reservation_flight FOREIGN KEY (id_flight) REFERENCES flight(id),
    CONSTRAINT chk_passenger_reservation_seat_number CHECK ( seat_number > 0 ),
    CONSTRAINT chk_passenger_reservation_status CHECK ( status IN ('RESERVED', 'CHECKED_IN', 'BOARDED', 'CANCELED') ),
    CONSTRAINT uk_passenger_reservation_passenger_reservation UNIQUE (id_passenger, id_reservation),
    CONSTRAINT uk_passenger_reservation_flight_seat_number UNIQUE (id_flight, seat_number)
);
CREATE INDEX idx_passenger_reservation_id_flight ON passenger_reservation(id_flight);
