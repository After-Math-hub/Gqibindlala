CREATE TABLE inventory_change_messages (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL REFERENCES inventory_change_requests(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES app_users(id),
    content VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_change_messages_thread
    ON inventory_change_messages (change_request_id, created_at);