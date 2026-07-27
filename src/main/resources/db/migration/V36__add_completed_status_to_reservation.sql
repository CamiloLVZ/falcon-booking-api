ALTER TABLE reservation
DROP CONSTRAINT IF EXISTS chk_reservation_status;

ALTER TABLE reservation ADD CONSTRAINT chk_reservation_status
    CHECK (status IN ('RESERVED', 'COMPLETED', 'CANCELED'));
