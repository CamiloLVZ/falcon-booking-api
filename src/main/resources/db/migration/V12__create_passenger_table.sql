CREATE TABLE passenger(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(1),
    id_country_nationality BIGINT NOT NULL,
    date_of_birth DATE NOT NULL,
    passport_number VARCHAR UNIQUE,
    identification_number VARCHAR(20),
    CONSTRAINT pk_passenger PRIMARY KEY(id),
    CONSTRAINT fk_passenger_country FOREIGN KEY (id_country_nationality) references country(id),
    CONSTRAINT uk_identification_number_id_country_nationality UNIQUE(identification_number, id_country_nationality),
    CONSTRAINT chk_passenger_gender CHECK ( gender IN ('M', 'F', 'O'))
);