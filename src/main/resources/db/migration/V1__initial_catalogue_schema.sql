-- Categories (self-referencing hierarchy)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES categories(id)
);

-- Items (CatalogueController / ItemRepository)
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(512),
    starting_price DOUBLE PRECISION NOT NULL,
    reserve_price DOUBLE PRECISION,
    current_price DOUBLE PRECISION NOT NULL,
    end_time TIMESTAMP NOT NULL,
    seller_id VARCHAR(255) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    has_bids BOOLEAN NOT NULL DEFAULT FALSE
);

-- Listings (ListingController / ListingRepository)
CREATE TABLE listings (
    id VARCHAR(255) PRIMARY KEY,
    seller_id VARCHAR(255),
    title VARCHAR(255),
    description TEXT,
    category VARCHAR(255),
    image_url VARCHAR(512),
    starting_price NUMERIC(19, 4),
    reserve_price NUMERIC(19, 4),
    current_price NUMERIC(19, 4),
    end_time TIMESTAMP,
    status VARCHAR(50)
);

