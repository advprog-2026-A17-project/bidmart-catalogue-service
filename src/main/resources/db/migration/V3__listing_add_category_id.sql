ALTER TABLE listings ADD COLUMN IF NOT EXISTS category_id BIGINT REFERENCES categories(id);
