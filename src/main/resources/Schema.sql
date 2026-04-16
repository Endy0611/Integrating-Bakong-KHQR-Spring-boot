-- 1. Create the table
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255),
    paid_at TIMESTAMP,
    qr_code TEXT,
    qr_md5 VARCHAR(32) UNIQUE,
    qr_expiration BIGINT,
    bakong_hash VARCHAR(255),
    from_account_id VARCHAR(100),
    to_account_id VARCHAR(100),
    description TEXT,
    paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Add an index for performance (matches your Sequelize index)
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at);

-- 3. Trigger to auto-update the 'updated_at' column
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ language 'plpgsql';
