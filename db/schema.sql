-- Game Market Database Schema (SQLite-friendly)
-- Use INTEGER for amounts in smallest currency unit (e.g., cents)

PRAGMA foreign_keys = ON;

-- Players
CREATE TABLE IF NOT EXISTS players (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER IF NOT EXISTS trg_players_updated
AFTER UPDATE ON players
FOR EACH ROW BEGIN
  UPDATE players SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- Currencies
CREATE TABLE IF NOT EXISTS currencies (
  code TEXT PRIMARY KEY, -- e.g., GOLD
  name TEXT NOT NULL,
  decimals INTEGER NOT NULL CHECK(decimals >= 0 AND decimals <= 9)
);

-- Wallets: one per player x currency
CREATE TABLE IF NOT EXISTS wallets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL,
  currency_code TEXT NOT NULL,
  balance INTEGER NOT NULL DEFAULT 0 CHECK(balance >= 0),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(player_id, currency_code),
  FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE,
  FOREIGN KEY(currency_code) REFERENCES currencies(code) ON DELETE RESTRICT
);

CREATE TRIGGER IF NOT EXISTS trg_wallets_updated
AFTER UPDATE ON wallets
FOR EACH ROW BEGIN
  UPDATE wallets SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- Ledger entries for audit (optional for balance derivation)
CREATE TABLE IF NOT EXISTS ledger_entries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  wallet_id INTEGER NOT NULL,
  amount INTEGER NOT NULL, -- signed
  reason TEXT NOT NULL, -- e.g., deposit|purchase|sale_proceeds|fee
  ref_table TEXT,
  ref_id INTEGER,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ledger_wallet ON ledger_entries(wallet_id, created_at);

-- Item types
CREATE TABLE IF NOT EXISTS item_types (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  rarity TEXT NOT NULL DEFAULT 'common',
  metadata TEXT
);

-- Items (non-fungible instances)
CREATE TABLE IF NOT EXISTS items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type_id INTEGER NOT NULL,
  owner_id INTEGER,
  status TEXT NOT NULL CHECK(status IN ('in_inventory','listed','sold','burned')),
  metadata TEXT,
  locked_by_listing_id INTEGER,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(type_id) REFERENCES item_types(id) ON DELETE RESTRICT,
  FOREIGN KEY(owner_id) REFERENCES players(id) ON DELETE SET NULL
);

CREATE TRIGGER IF NOT EXISTS trg_items_updated
AFTER UPDATE ON items
FOR EACH ROW BEGIN
  UPDATE items SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

CREATE INDEX IF NOT EXISTS idx_items_owner ON items(owner_id);
CREATE INDEX IF NOT EXISTS idx_items_status ON items(status);

-- Listings (fixed-price for MVP)
CREATE TABLE IF NOT EXISTS listings (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  item_id INTEGER NOT NULL UNIQUE,
  seller_id INTEGER NOT NULL,
  currency_code TEXT NOT NULL,
  price INTEGER NOT NULL CHECK(price > 0),
  status TEXT NOT NULL CHECK(status IN ('active','sold','cancelled','expired')) DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME,
  FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE RESTRICT,
  FOREIGN KEY(seller_id) REFERENCES players(id) ON DELETE RESTRICT,
  FOREIGN KEY(currency_code) REFERENCES currencies(code) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status, created_at);

-- Orders (completed purchases)
CREATE TABLE IF NOT EXISTS orders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  listing_id INTEGER NOT NULL,
  buyer_id INTEGER NOT NULL,
  currency_code TEXT NOT NULL,
  price_paid INTEGER NOT NULL,
  fee_amount INTEGER NOT NULL DEFAULT 0,
  seller_receipt INTEGER NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(listing_id) REFERENCES listings(id) ON DELETE RESTRICT,
  FOREIGN KEY(buyer_id) REFERENCES players(id) ON DELETE RESTRICT,
  FOREIGN KEY(currency_code) REFERENCES currencies(code) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_listing ON orders(listing_id);

-- Simple settings table (e.g., marketplace fee in basis points)
CREATE TABLE IF NOT EXISTS settings (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL
);
