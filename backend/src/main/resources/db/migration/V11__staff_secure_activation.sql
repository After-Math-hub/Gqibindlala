ALTER TABLE app_users ADD COLUMN staff_activated BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE staff_activation_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP
);

CREATE INDEX idx_staff_activation_user ON staff_activation_tokens (user_id, expires_at);