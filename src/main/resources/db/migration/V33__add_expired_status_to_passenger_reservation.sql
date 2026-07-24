ALTER TABLE passenger_reservation
DROP CONSTRAINT chk_passenger_reservation_status;

ALTER TABLE passenger_reservation ADD CONSTRAINT chk_passenger_reservation_status
        CHECK (status IN ('RESERVED', 'CHECKED_IN', 'BOARDED', 'EXPIRED', 'CANCELED'));
