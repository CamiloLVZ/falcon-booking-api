CREATE TABLE payment (
  id                 BIGSERIAL PRIMARY KEY,
  reservation_number VARCHAR(12) NOT NULL REFERENCES reservation(number),
  amount             DECIMAL(10,2) NOT NULL,
  status             VARCHAR(20)   NOT NULL,
  created_at         TIMESTAMP     NOT NULL
);
