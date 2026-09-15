CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    total_quantity INTEGER NOT NULL,
    price_per_unit_per_day NUMERIC(10, 2) NOT NULL,
    image_url VARCHAR(500)
);

CREATE TABLE booking_requests (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_contact VARCHAR(255) NOT NULL,
    event_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE booking_items (
    id BIGSERIAL PRIMARY KEY,
    booking_request_id BIGINT NOT NULL REFERENCES booking_requests(id) ON DELETE CASCADE,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    quantity INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    line_total NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

-- Speeds up the overlapping-date-range availability query
CREATE INDEX idx_booking_items_equipment_dates
    ON booking_items (equipment_id, status, start_date, end_date);

-- Seed starting equipment catalogue
INSERT INTO equipment (name, category, description, total_quantity, price_per_unit_per_day) VALUES
    ('Plastic Chair', 'CHAIR', 'Standard white plastic event chair', 200, 5.00),
    ('Round Table (10-seater)', 'TABLE', 'Round table, seats up to 10', 30, 50.00),
    ('Marquee Tent (6x9m)', 'TENT', 'Waterproof marquee tent, 6x9 metres', 8, 800.00),
    ('Mobile Fridge', 'MOBILE_FRIDGE', 'Trailer-mounted mobile fridge unit', 1, 600.00),
    ('Meat Cutter', 'MEAT_CUTTER', 'Commercial meat cutting/mincing machine', 2, 250.00);
