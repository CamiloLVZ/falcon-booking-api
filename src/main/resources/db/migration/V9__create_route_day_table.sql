CREATE TABLE route_day (
                           id BIGINT GENERATED ALWAYS AS IDENTITY,
                           id_route BIGINT NOT NULL,
                           week_day VARCHAR(10) NOT NULL,
                           CONSTRAINT fk_route_day_route FOREIGN KEY (id_route) REFERENCES route(id),
                           CONSTRAINT uk_route_day_route_week_day UNIQUE (id_route, week_day),
                           CONSTRAINT chk_route_week_day
                                CHECK (week_day IN ('MONDAY',
                                                    'TUESDAY',
                                                    'WEDNESDAY',
                                                    'THURSDAY',
                                                    'FRIDAY',
                                                    'SATURDAY',
                                                    'SUNDAY'))
);