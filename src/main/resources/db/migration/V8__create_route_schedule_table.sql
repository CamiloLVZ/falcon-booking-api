CREATE TABLE route_schedule (
                                id BIGINT GENERATED ALWAYS AS IDENTITY,
                                id_route BIGINT NOT NULL,
                                departure_local_time TIME NOT NULL,
                                CONSTRAINT fk_route_schedule_route FOREIGN KEY (id_route) REFERENCES route(id),
                                CONSTRAINT uk_route_schedule_route_time UNIQUE (id_route, departure_local_time)
);