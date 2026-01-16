ALTER TABLE airport ADD COLUMN timezone VARCHAR(50);

UPDATE airport
SET timezone = CASE
    -- ESTADOS UNIDOS (id_country = 1) - Agrupados por zona horaria común
                   WHEN id_country = 1 AND iata_code IN ('JFK', 'EWR', 'BOS', 'PHL', 'IAD', 'ATL', 'MCO', 'MIA', 'DTW') THEN 'America/New_York'
                   WHEN id_country = 1 AND iata_code IN ('ORD', 'DFW', 'IAH', 'MSP') THEN 'America/Chicago'
                   WHEN id_country = 1 AND iata_code IN ('DEN', 'SLC') THEN 'America/Denver'
                   WHEN id_country = 1 AND iata_code IN ('PHX') THEN 'America/Phoenix'
                   WHEN id_country = 1 AND iata_code IN ('LAX', 'SFO', 'SEA', 'LAS') THEN 'America/Los_Angeles'

    -- CANADÁ (id_country = 2)
                   WHEN id_country = 2 AND iata_code IN ('YYZ', 'YUL', 'YOW') THEN 'America/Toronto'
                   WHEN id_country = 2 AND iata_code IN ('YYC', 'YEG') THEN 'America/Edmonton'
                   WHEN id_country = 2 AND iata_code = 'YVR' THEN 'America/Vancouver'

    -- MÉXICO (id_country = 3)
                   WHEN id_country = 3 AND iata_code IN ('MEX', 'CUN', 'GDL', 'MTY') THEN 'America/Mexico_City'
                   WHEN id_country = 3 AND iata_code = 'TIJ' THEN 'America/Tijuana'

    -- CENTROAMÉRICA Y CARIBE
                   WHEN id_country = 4 THEN 'America/Panama'        -- PTY
                   WHEN id_country = 5 THEN 'America/Costa_Rica'    -- SJO
                   WHEN id_country = 6 THEN 'America/El_Salvador'   -- SAL
                   WHEN id_country = 7 THEN 'America/Guatemala'     -- GUA
                   WHEN id_country = 8 THEN 'America/Tegucigalpa'   -- SAP
                   WHEN id_country = 9 THEN 'America/Managua'       -- MGA
                   WHEN id_country = 10 THEN 'America/Belize'       -- BZE
                   WHEN id_country = 11 THEN 'America/Puerto_Rico'  -- SJU
                   WHEN id_country = 12 THEN 'America/Santo_Domingo'-- PUJ, SDQ
                   WHEN id_country = 13 THEN 'America/Havana'       -- HAV
                   WHEN id_country = 14 THEN 'America/Jamaica'      -- KIN
                   WHEN id_country = 15 THEN 'America/Nassau'       -- NAS
                   WHEN id_country = 16 THEN 'America/Port-au-Prince'-- PAP
                   WHEN id_country = 17 THEN 'America/Barbados'      -- BGI
                   WHEN id_country = 18 THEN 'America/Port_of_Spain' -- POS
                   WHEN id_country = 19 THEN 'America/Lower_Princes' -- SXM

    -- COLOMBIA (id_country = 20)
                   WHEN id_country = 20 THEN 'America/Bogota'

    -- BRASIL (id_country = 21)
                   WHEN id_country = 21 THEN 'America/Sao_Paulo'

    -- PERÚ (id_country = 22)
                   WHEN id_country = 22 THEN 'America/Lima'

    -- CHILE (id_country = 23)
                   WHEN id_country = 23 THEN 'America/Santiago'

    -- ARGENTINA (id_country = 24)
                   WHEN id_country = 24 THEN 'America/Argentina/Buenos_Aires'

    -- ECUADOR (id_country = 25)
                   WHEN id_country = 25 THEN 'America/Guayaquil'

    -- VENEZUELA (id_country = 26)
                   WHEN id_country = 26 THEN 'America/Caracas'

    -- BOLIVIA (id_country = 27)
                   WHEN id_country = 27 THEN 'America/La_Paz'

    -- PARAGUAY (id_country = 28)
                   WHEN id_country = 28 THEN 'America/Asuncion'

    -- URUGUAY (id_country = 29)
                   WHEN id_country = 29 THEN 'America/Montevideo'

    -- OTROS SURAMÉRICA
                   WHEN id_country = 30 THEN 'America/Guyana'       -- GEO
                   WHEN id_country = 31 THEN 'America/Paramaribo'   -- PBM
                   WHEN id_country = 32 THEN 'America/Cayenne'      -- CAY

                   ELSE 'UTC' -- Valor por defecto si no coincide ninguno
    END;

ALTER TABLE airport ALTER COLUMN timezone SET NOT NULL;
