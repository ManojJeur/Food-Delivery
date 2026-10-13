CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(254) NOT NULL,
    address VARCHAR(300) NOT NULL,
    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CLOSED',
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_restaurants_status CHECK (status IN ('OPEN', 'CLOSED', 'TEMPORARILY_UNAVAILABLE', 'SUSPENDED')),
    CONSTRAINT ck_restaurants_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT ck_restaurants_longitude CHECK (longitude >= -180 AND longitude <= 180),
    CONSTRAINT ck_restaurants_hours CHECK (opening_time <> closing_time)
);

CREATE INDEX idx_restaurants_owner_id ON restaurants (owner_id);
CREATE INDEX idx_restaurants_status_name ON restaurants (status, name);
