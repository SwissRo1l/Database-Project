-- PostgreSQL schema for Game Market

CREATE TABLE IF NOT EXISTS players (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS currencies (
  code VARCHAR(16) PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  decimals SMALLINT NOT NULL CHECK (decimals BETWEEN 0 AND 9)
);

CREATE TABLE IF NOT EXISTS wallets (
  id BIGSERIAL PRIMARY KEY,
  player_id BIGINT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  currency_code VARCHAR(16) NOT NULL REFERENCES currencies(code) ON DELETE RESTRICT,
  balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(player_id, currency_code)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  wallet_id BIGINT NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
  amount BIGINT NOT NULL,
  reason VARCHAR(32) NOT NULL,
  ref_table VARCHAR(64),
  ref_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ledger_wallet ON ledger_entries(wallet_id, created_at);

CREATE TABLE IF NOT EXISTS item_types (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE,
  rarity VARCHAR(32) NOT NULL DEFAULT 'common',
  metadata JSONB
);

CREATE TABLE IF NOT EXISTS items (
  id BIGSERIAL PRIMARY KEY,
  type_id BIGINT NOT NULL REFERENCES item_types(id) ON DELETE RESTRICT,
  owner_id BIGINT REFERENCES players(id) ON DELETE SET NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('in_inventory','listed','sold','burned')),
  metadata JSONB,
  locked_by_listing_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_items_owner ON items(owner_id);
CREATE INDEX IF NOT EXISTS idx_items_status ON items(status);

CREATE TABLE IF NOT EXISTS listings (
  id BIGSERIAL PRIMARY KEY,
  item_id BIGINT NOT NULL UNIQUE REFERENCES items(id) ON DELETE RESTRICT,
  seller_id BIGINT NOT NULL REFERENCES players(id) ON DELETE RESTRICT,
  currency_code VARCHAR(16) NOT NULL REFERENCES currencies(code) ON DELETE RESTRICT,
  price BIGINT NOT NULL CHECK (price > 0),
  status VARCHAR(16) NOT NULL DEFAULT 'active' CHECK (status IN ('active','sold','cancelled','expired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status, created_at);

CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE RESTRICT,
  buyer_id BIGINT NOT NULL REFERENCES players(id) ON DELETE RESTRICT,
  currency_code VARCHAR(16) NOT NULL REFERENCES currencies(code) ON DELETE RESTRICT,
  price_paid BIGINT NOT NULL,
  fee_amount BIGINT NOT NULL DEFAULT 0,
  seller_receipt BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_listing ON orders(listing_id);

CREATE TABLE IF NOT EXISTS settings (
  key VARCHAR(64) PRIMARY KEY,
  value TEXT NOT NULL
);

-- seed currency and fee
INSERT INTO currencies(code, name, decimals) VALUES ('GOLD','Gold Coin',2)
  ON CONFLICT (code) DO NOTHING;
INSERT INTO settings(key, value) VALUES ('market_fee_bps','250')
  ON CONFLICT (key) DO NOTHING;
