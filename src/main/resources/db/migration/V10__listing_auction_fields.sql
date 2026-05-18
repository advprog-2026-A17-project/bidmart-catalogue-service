ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS minimum_increment NUMERIC(19, 4);

ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;

UPDATE listings
SET minimum_increment = 1
WHERE minimum_increment IS NULL;
