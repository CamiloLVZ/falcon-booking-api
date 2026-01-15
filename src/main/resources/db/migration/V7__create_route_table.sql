CREATE TABLE route(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    id_airport_origin BIGINT NOT NULL,
    id_airport_destination BIGINT NOT NULL,
    flight_number VARCHAR(8) NOT NULL,
    id_default_airplane_type BIGINT NOT NULL,
    length_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_route PRIMARY KEY (id),
    CONSTRAINT fk_route_airport_origin FOREIGN KEY (id_airport_origin) REFERENCES airport (id),
    CONSTRAINT fk_route_airport_destination FOREIGN KEY (id_airport_destination) REFERENCES airport (id),
    CONSTRAINT fk_route_default_airplane_type FOREIGN KEY (id_default_airplane_type) REFERENCES airplane_type (id),
    CONSTRAINT uk_flight_number UNIQUE (flight_number),
    CONSTRAINT chk_route_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_route_length_minutes_positive CHECK(length_minutes>0),
    CONSTRAINT chk_airport_origin_different_airport_destination CHECK(id_airport_origin != id_airport_destination)
);

CREATE INDEX idx_route_origin_destination ON route (id_airport_origin, id_airport_destination);
