ALTER TABLE app_users ADD COLUMN personal_email VARCHAR(255);
UPDATE app_users SET personal_email = login_identifier WHERE role = 'CUSTOMER';

CREATE TABLE order_messages (
    id BIGSERIAL PRIMARY KEY,
    booking_request_id BIGINT NOT NULL REFERENCES booking_requests(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES app_users(id),
    department_id BIGINT REFERENCES departments(id),
    content VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_messages_thread ON order_messages (booking_request_id, created_at);