ALTER TABLE departments ADD COLUMN catalog_visible BOOLEAN NOT NULL DEFAULT TRUE;
UPDATE departments SET catalog_visible = FALSE WHERE slug = 'finance';

ALTER TABLE booking_requests ADD COLUMN finance_request_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUESTED';

CREATE INDEX idx_booking_requests_finance_status ON booking_requests (finance_request_status, created_at);