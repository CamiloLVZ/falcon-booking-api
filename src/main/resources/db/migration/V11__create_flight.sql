CREATE TABLE flight(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    id_route BIGINT NOT NULL,
    departure_datetime TIMESTAMPTZ NOT NULL,
    id_airplane_type BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_flight PRIMARY KEY (id),
    CONSTRAINT fk_flight_route FOREIGN KEY (id_route) REFERENCES route (id),
    CONSTRAINT fk_flight_airplane_type FOREIGN KEY (id_airplane_type) REFERENCES airplane_type (id),
    CONSTRAINT uk_id_route_departure_datetime UNIQUE(id_route, departure_datetime),
    CONSTRAINT chk_flight_status CHECK (status IN ('SCHEDULED', 'BOARDING', 'COMPLETED', 'CANCELED'))
);

