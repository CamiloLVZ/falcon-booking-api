ALTER TABLE flight
  ADD COLUMN base_price_economy     DECIMAL(10,2) NOT NULL DEFAULT 0,
  ADD COLUMN base_price_first_class DECIMAL(10,2) NOT NULL DEFAULT 0;
