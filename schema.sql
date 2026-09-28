CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL CHECK (length(trim(name)) > 0),
    sku TEXT NOT NULL UNIQUE CHECK (length(trim(sku)) > 0),
    category TEXT NOT NULL CHECK (length(trim(category)) > 0),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit_price REAL NOT NULL CHECK (unit_price >= 0),
    reorder_threshold INTEGER NOT NULL DEFAULT 0 CHECK (reorder_threshold >= 0)
);

CREATE TABLE IF NOT EXISTS stock_transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    transaction_type TEXT NOT NULL
        CHECK (transaction_type IN ('STOCK_IN', 'STOCK_OUT')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    transaction_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
