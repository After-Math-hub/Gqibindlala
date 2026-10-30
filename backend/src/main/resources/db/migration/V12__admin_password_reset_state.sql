CREATE TABLE admin_password_reset_state (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    reset_at TIMESTAMPTZ NOT NULL
);
