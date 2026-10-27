ALTER TABLE app_users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_app_users_staff_active ON app_users (role, active, full_name);