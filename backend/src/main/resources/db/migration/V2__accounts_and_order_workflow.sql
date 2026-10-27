CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_app_users_department_role CHECK (
        (role = 'DEPARTMENT' AND department IS NOT NULL)
        OR (role <> 'DEPARTMENT' AND department IS NULL)
    )
);

ALTER TABLE booking_requests
    ADD COLUMN customer_id BIGINT REFERENCES app_users(id),
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN payment_reference VARCHAR(255),
    ADD COLUMN payment_reviewed_at TIMESTAMP,
    ADD COLUMN payment_reviewed_by_id BIGINT REFERENCES app_users(id);

ALTER TABLE booking_items
    ADD COLUMN fulfillment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED';

UPDATE booking_requests SET payment_status = 'PAID' WHERE status = 'CONFIRMED';
UPDATE booking_requests SET payment_status = 'CANCELLED' WHERE status = 'CANCELLED';

CREATE INDEX idx_booking_requests_customer ON booking_requests (customer_id, created_at DESC);
CREATE INDEX idx_booking_requests_payment ON booking_requests (payment_status, created_at);