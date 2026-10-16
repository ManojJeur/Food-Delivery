CREATE TABLE menu_categories (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL DEFAULT '',
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_menu_categories_id_restaurant UNIQUE (id, restaurant_id),
    CONSTRAINT ck_menu_categories_display_order CHECK (display_order >= 0)
);

CREATE UNIQUE INDEX uq_menu_categories_restaurant_name
    ON menu_categories (restaurant_id, lower(name));
CREATE INDEX idx_menu_categories_restaurant_active_order
    ON menu_categories (restaurant_id, active, display_order);

CREATE TABLE menu_items (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    category_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL DEFAULT '',
    price NUMERIC(10, 2) NOT NULL,
    image_url VARCHAR(2048),
    vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    preparation_time_minutes SMALLINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_menu_items_category_restaurant
        FOREIGN KEY (category_id, restaurant_id)
        REFERENCES menu_categories (id, restaurant_id),
    CONSTRAINT ck_menu_items_price CHECK (price > 0),
    CONSTRAINT ck_menu_items_preparation_time CHECK (preparation_time_minutes BETWEEN 1 AND 240)
);

CREATE INDEX idx_menu_items_restaurant_available
    ON menu_items (restaurant_id, active, available, name);
CREATE INDEX idx_menu_items_category
    ON menu_items (category_id, active, available, name);
