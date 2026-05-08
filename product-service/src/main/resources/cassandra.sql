
CREATE KEYSPACE ecommerce WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE ecommerce;

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name text,
    price decimal
);

INSERT INTO products (id, name, price) VALUES (uuid(), 'Sample Product', 19.99);

SELECT * FROM products;