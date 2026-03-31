ALTER TABLE flight_generation
DROP CONSTRAINT chk_flight_generation_type;

ALTER TABLE flight_generation
    ADD CONSTRAINT chk_flight_generation_type
        CHECK (type IN ('GLOBAL', 'ROUTE', 'DAILY'));

ALTER TABLE flight_generation ADD
 target_date DATE;

ALTER TABLE flight_generation ADD CONSTRAINT chk_flight_generation_target_date
        CHECK (
        (type = 'DAILY' AND target_date IS NOT NULL)
        OR (type <> 'DAILY' AND target_date IS NULL));

CREATE UNIQUE INDEX idx_flight_generation_only_one_running
    ON flight_generation (status)
    WHERE status = 'RUNNING';


