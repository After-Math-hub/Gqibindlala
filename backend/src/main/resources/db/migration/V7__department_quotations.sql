ALTER TABLE booking_items
    ADD COLUMN quotation_issued_at TIMESTAMP,
    ADD COLUMN quotation_issued_by_id BIGINT REFERENCES app_users(id);

CREATE INDEX idx_booking_items_quotation ON booking_items (booking_request_id, quotation_issued_at);