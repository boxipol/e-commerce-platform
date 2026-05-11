// local
CREATE KEYSPACE ecommerce WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE ecommerce;

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name text,
    price decimal
);

CREATE TABLE products_by_id (
    product_id UUID PRIMARY KEY,

    sku TEXT,
    name TEXT,
    description TEXT,

    brand TEXT,
    category TEXT,

    price DECIMAL,
    currency TEXT,

    stock INT,

    active BOOLEAN,

    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

SELECT * FROM products_by_id
WHERE product_id = ?;

SELECT product_id, sku, name
FROM products_by_id
LIMIT 10;


CREATE TABLE products_by_category (
    category TEXT,
    created_at TIMESTAMP,
    product_id UUID,

    name TEXT,
    brand TEXT,
    price DECIMAL,
    stock INT,

    PRIMARY KEY ((category), created_at, product_id)
)
WITH CLUSTERING ORDER BY (created_at DESC);

SELECT * FROM products_by_category
WHERE category = 'Phones';



CREATE TABLE product_variants_by_product (
    product_id UUID,
    variant_id UUID,

    sku TEXT,

    color TEXT,
    size TEXT,

    price DECIMAL,
    stock INT,

    PRIMARY KEY ((product_id), variant_id)
);

SELECT * FROM product_variants_by_product
WHERE product_id = ?;


INSERT INTO products (id, name, price) VALUES (uuid(), 'Sample Product', 19.99);

SELECT * FROM products;





// production
CREATE KEYSPACE ecommerce

WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'datacenter1': 3
};