CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE orders
(
    id              UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    public_order_id VARCHAR(40)              NOT NULL UNIQUE,
    user_id         UUID                     NOT NULL,
    user_mail       VARCHAR(255)             NOT NULL,
    status          VARCHAR(30)              NOT NULL,
    total_amount    NUMERIC(12, 2)           NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_id
    ON orders (user_id);

CREATE INDEX idx_orders_public_order_id
    ON orders (public_order_id);

CREATE TABLE order_items
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID           NOT NULL,
    product_id UUID           NOT NULL,
    sku        VARCHAR(100)   NOT NULL,
    quantity   INT            NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal   NUMERIC(12, 2) NOT NULL,

    CONSTRAINT fk_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);

CREATE TABLE outbox_events
(
    id             UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100)             NOT NULL,
    aggregate_id   UUID                     NOT NULL,
    event_type     VARCHAR(100)             NOT NULL,
    payload        JSONB                    NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    published_at   TIMESTAMP WITH TIME ZONE,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_status_created
    ON outbox_events (status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_events (aggregate_id, event_type);

CREATE UNIQUE INDEX IF NOT EXISTS ux_outbox_dedup
    ON outbox_events (aggregate_id, event_type, status);