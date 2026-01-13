ALTER TABLE airplane_type
    ADD COLUMN status VARCHAR(20);

UPDATE airplane_type
SET status = 'ACTIVE';

ALTER TABLE airplane_type
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE airplane_type
    ADD CONSTRAINT chk_airplane_type_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'RETIRED'));
