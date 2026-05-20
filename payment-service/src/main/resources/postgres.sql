
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    order_id   UUID           NOT NULL,
    user_id    UUID           NOT NULL,

    amount     NUMERIC(19, 2) NOT NULL,
    currency   VARCHAR(10)    NOT NULL,

    status     VARCHAR(30)    NOT NULL,

    provider   VARCHAR(30),

    created_at TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at TIMESTAMP      NOT NULL DEFAULT now()
);