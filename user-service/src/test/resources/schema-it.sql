CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    email      TEXT      NOT NULL UNIQUE,
    password   TEXT      NOT NULL,
    role       TEXT      NOT NULL,
    first_name TEXT,
    last_name  TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
