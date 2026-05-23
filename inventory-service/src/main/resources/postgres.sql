
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE inventory
(
    product_id UUID PRIMARY KEY,
    stock      INT       NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL,
    version    BIGINT    NOT NULL DEFAULT 0
);