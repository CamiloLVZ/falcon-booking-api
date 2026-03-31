CREATE TABLE flight_generation(
                       id BIGINT GENERATED ALWAYS AS IDENTITY,
                       id_route BIGINT,
                       type VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       started_at TIMESTAMPTZ NOT NULL,
                       finished_at TIMESTAMPTZ,
                       total_generated INT,
                       CONSTRAINT pk_flight_generation PRIMARY KEY (id),
                       CONSTRAINT fk_flight_generation_route FOREIGN KEY (id_route) REFERENCES route (id),
                       CONSTRAINT chk_flight_generation_type CHECK (type IN ('GLOBAL', 'ROUTE')),
                       CONSTRAINT chk_flight_generation_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED')),
                       CONSTRAINT chk_route_required_for_route_flight_generation
                           CHECK (
                               (type = 'GLOBAL' AND id_route IS NULL) OR
                               (type = 'ROUTE' AND id_route IS NOT NULL))
);

