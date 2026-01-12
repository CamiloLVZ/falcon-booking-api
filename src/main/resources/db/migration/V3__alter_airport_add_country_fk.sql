--REPLACE STATIC COLUMN country TO FOREIGN KEY id_country
ALTER TABLE airport ADD COLUMN id_country BIGINT NOT NULL;

ALTER TABLE airport ADD CONSTRAINT fk_airport_country FOREIGN KEY (id_country) REFERENCES country (id);

ALTER TABLE airport DROP COLUMN IF EXISTS country;