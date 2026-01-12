--TABLE AIRPORT
CREATE TABLE airport (
                         id BIGINT GENERATED ALWAYS AS IDENTITY,
                         name VARCHAR(150) NOT NULL,
                         country VARCHAR(100) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         iata_code CHAR(3) NOT NULL,
                         CONSTRAINT pk_airport PRIMARY KEY (id),
                         CONSTRAINT uk_airport_iata_code UNIQUE (iata_code)
);

--TABLE AIRPLANE_TYPE
CREATE TABLE airplane_type (
        id BIGINT GENERATED ALWAYS AS IDENTITY,
        producer VARCHAR(100) NOT NULL,
        model VARCHAR(100) NOT NULL,
        economy_seats INTEGER NOT NULL,
        first_class_seats INTEGER NOT NULL,
        CONSTRAINT pk_airplane_type PRIMARY KEY (id),
        CONSTRAINT uk_airplane_type_producer_model UNIQUE (producer, model),
        CONSTRAINT chk_airplane_type_economy_seats_positive CHECK(economy_seats>0),
        CONSTRAINT chk_airplane_type_first_class_seats_non_negative CHECK(first_class_seats>=0)
);

--TABLE WEEK_DAY
CREATE TABLE week_day (
        id SMALLINT GENERATED ALWAYS AS IDENTITY,
        day_index SMALLINT NOT NULL,
        name VARCHAR(9) NOT NULL,
        CONSTRAINT pk_week_day PRIMARY KEY (id),
        CONSTRAINT uk_week_day_day_index UNIQUE (day_index),
        CONSTRAINT uk_week_day_name UNIQUE (name),
        CONSTRAINT chk_week_day_index_range CHECK (day_index BETWEEN 1 AND 7)
);

INSERT INTO week_day (day_index, name) VALUES (1, 'MONDAY');
INSERT INTO week_day (day_index, name) VALUES (2, 'TUESDAY');
INSERT INTO week_day (day_index, name) VALUES (3, 'WEDNESDAY');
INSERT INTO week_day (day_index, name) VALUES (4, 'THURSDAY');
INSERT INTO week_day (day_index, name) VALUES (5, 'FRIDAY');
INSERT INTO week_day (day_index, name) VALUES (6, 'SATURDAY');
INSERT INTO week_day (day_index, name) VALUES (7, 'SUNDAY');