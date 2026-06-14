// local
CREATE KEYSPACE ecommerce
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };

// production
CREATE KEYSPACE ecommerce
    WITH replication = {
        'class': 'NetworkTopologyStrategy',
        'datacenter1': 3
        };

USE ecommerce;

CREATE TABLE products
(
    id    UUID PRIMARY KEY,
    name  text,
    price decimal
);

CREATE TABLE products_by_id
(
    product_id  UUID PRIMARY KEY,

    sku         TEXT,
    name        TEXT,
    description TEXT,

    brand       TEXT,
    category    TEXT,

    price       DECIMAL,
    currency    TEXT,

    stock       INT,

    active      BOOLEAN,

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE products_by_sku
(
    sku         TEXT PRIMARY KEY,

    product_id  UUID,

    name        TEXT,
    description TEXT,

    brand       TEXT,
    category    TEXT,

    price       DECIMAL,
    currency    TEXT,

    stock       INT,
    active      BOOLEAN,

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE products_by_category
(
    category   TEXT,
    created_at TIMESTAMP,
    sku        TEXT,

    name       TEXT,
    brand      TEXT,
    price      DECIMAL,
    stock      INT,

    PRIMARY KEY ((category), created_at, sku)
)
            WITH CLUSTERING ORDER BY (created_at DESC);