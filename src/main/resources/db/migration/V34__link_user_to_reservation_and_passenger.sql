ALTER TABLE reservation ADD COLUMN id_user BIGINT;
ALTER TABLE reservation ADD CONSTRAINT fk_reservation_user FOREIGN KEY (id_user) REFERENCES users (id);
CREATE INDEX idx_reservation_id_user ON reservation(id_user);

ALTER TABLE passenger ADD COLUMN id_user BIGINT UNIQUE;
ALTER TABLE passenger ADD CONSTRAINT fk_passenger_user FOREIGN KEY (id_user) REFERENCES users (id);
