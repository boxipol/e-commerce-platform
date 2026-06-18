CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID                        NOT NULL,
    public_order_id     VARCHAR(40)                 NOT NULL,
    user_id             UUID                        NOT NULL,
    user_mail           VARCHAR(255)                NOT NULL,
    amount              NUMERIC(19, 2)              NOT NULL,
    items               JSONB                       NOT NULL,
    currency            VARCHAR(10)                 NOT NULL,
    status              VARCHAR(32)                 NOT NULL,
    provider            VARCHAR(32)                 NOT NULL,
    provider_payment_id VARCHAR(128),
    payment_url         TEXT,
    failure_reason      TEXT,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_order_id
    ON payments (order_id);

CREATE INDEX idx_payments_public_order_id
    ON payments (public_order_id);

CREATE INDEX idx_payments_user_id
    ON payments (user_id);

CREATE INDEX idx_payments_status
    ON payments (status);

CREATE INDEX idx_payments_provider_payment_id
    ON payments (provider_payment_id);