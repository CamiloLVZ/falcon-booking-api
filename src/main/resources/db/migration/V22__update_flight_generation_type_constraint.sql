ALTER TABLE flight_generation
DROP CONSTRAINT IF EXISTS chk_route_required_for_route_flight_generation;

UPDATE flight_generation SET id_route = NULL WHERE type IN ('GLOBAL', 'DAILY');


ALTER TABLE flight_generation
    ADD CONSTRAINT chk_route_required_for_route_flight_generation
        CHECK (
            (type IN ('GLOBAL', 'DAILY') AND id_route IS NULL) OR
            (type = 'ROUTE' AND id_route IS NOT NULL)
            );