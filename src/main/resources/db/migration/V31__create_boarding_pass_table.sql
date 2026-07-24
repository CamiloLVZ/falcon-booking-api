CREATE TABLE boarding_pass
(
    id                       BIGINT GENERATED ALWAYS AS IDENTITY,
    id_passenger_reservation BIGINT      NOT NULL,
    qr_token                 UUID        NOT NULL,
    status                   VARCHAR(20) NOT NULL,
    generated_at             TIMESTAMPTZ NOT NULL,
    emailed_at               TIMESTAMPTZ,
    boarded_at               TIMESTAMPTZ,
    CONSTRAINT pk_boarding_pass PRIMARY KEY (id),
    CONSTRAINT fk_boarding_pass_passenger_reservation FOREIGN KEY (id_passenger_reservation) REFERENCES passenger_reservation (id),
    CONSTRAINT chk_boarding_pass_status CHECK ( status IN ('ISSUED', 'BOARDED','EXPIRED') ),
    CONSTRAINT uk_boarding_pass_passenger_reservation UNIQUE (id_passenger_reservation),
    CONSTRAINT uk_boarding_pass_qr_token UNIQUE (qr_token)
);

CREATE INDEX idx_boarding_pass_status ON boarding_pass(status);