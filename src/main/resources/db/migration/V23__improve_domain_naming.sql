ALTER TABLE route RENAME COLUMN length_minutes TO duration_minutes;

ALTER TABLE reservation RENAME COLUMN datetime_reservation TO reservation_datetime;

ALTER TABLE route_day RENAME COLUMN week_day TO day_of_week;