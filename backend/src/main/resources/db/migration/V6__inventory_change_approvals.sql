CREATE TABLE inventory_change_requests (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    equipment_id BIGINT REFERENCES equipment(id),
    requested_by_id BIGINT NOT NULL REFERENCES app_users(id),
    reviewed_by_id BIGINT REFERENCES app_users(id),
    change_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    proposed_name VARCHAR(255),
    proposed_description VARCHAR(1000),
    proposed_quantity INTEGER,
    proposed_price NUMERIC(10, 2),
    proposed_image_url VARCHAR(500),
    reason VARCHAR(1000) NOT NULL,
    review_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMP,
    CONSTRAINT chk_inventory_change_request_type CHECK (change_type IN ('ADD_EQUIPMENT', 'PRICE_CHANGE', 'STOCK_CHANGE')),
    CONSTRAINT chk_inventory_change_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_inventory_change_request_payload CHECK (
        (change_type = 'ADD_EQUIPMENT' AND equipment_id IS NULL AND proposed_name IS NOT NULL
            AND proposed_quantity IS NOT NULL AND proposed_price IS NOT NULL AND proposed_image_url IS NOT NULL)
        OR (change_type = 'PRICE_CHANGE' AND equipment_id IS NOT NULL AND proposed_price IS NOT NULL)
        OR (change_type = 'STOCK_CHANGE' AND equipment_id IS NOT NULL AND proposed_quantity IS NOT NULL)
    )
);

CREATE INDEX idx_inventory_change_status ON inventory_change_requests (status, created_at);
CREATE INDEX idx_inventory_change_department ON inventory_change_requests (department_id, created_at DESC);