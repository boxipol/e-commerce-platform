
CREATE TABLE inventory
(
    product_id UUID PRIMARY KEY,
    quantity   INTEGER NOT NULL CHECK (quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO inventory (
    product_id,
    quantity
)
VALUES
    (
        '56308c75-a5ff-4726-b51b-fa7fbae18c3e',
        100
    ),
    (
        'cac72b5b-78da-4a83-9b6f-1ccf2acfe646',
        50
    );