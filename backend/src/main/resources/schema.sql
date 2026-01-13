CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================= MERCHANTS =================
CREATE TABLE IF NOT EXISTS merchants (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  api_key VARCHAR(64) UNIQUE NOT NULL,
  api_secret VARCHAR(64) NOT NULL,
  webhook_url TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================= ORDERS =================
CREATE TABLE IF NOT EXISTS orders (
  id VARCHAR(64) PRIMARY KEY,
  merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
  amount INTEGER NOT NULL CHECK (amount >= 100),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  receipt VARCHAR(255),                 -- ✅ optional (FIXED)
  notes JSONB,
  status VARCHAR(20) NOT NULL DEFAULT 'created',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ================= PAYMENTS =================
CREATE TABLE IF NOT EXISTS payments (
  id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
  amount INTEGER NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  method VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'created', -- ✅ DEFAULT ADDED
  vpa VARCHAR(255),
  card_network VARCHAR(20),
  card_last4 VARCHAR(4),
  error_code VARCHAR(50),
  error_description TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ================= INDEXES =================
CREATE INDEX IF NOT EXISTS idx_orders_merchant
ON orders(merchant_id);

CREATE INDEX IF NOT EXISTS idx_payments_order
ON payments(order_id);

CREATE INDEX IF NOT EXISTS idx_payments_status
ON payments(status);