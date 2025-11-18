-- Seed data for quick demo

INSERT OR IGNORE INTO currencies(code, name, decimals) VALUES
  ('GOLD', 'Gold Coin', 2);

INSERT OR IGNORE INTO settings(key, value) VALUES
  ('market_fee_bps', '250'); -- 2.5%

-- Players
INSERT OR IGNORE INTO players(username) VALUES ('alice');
INSERT OR IGNORE INTO players(username) VALUES ('bob');

-- Wallets (auto create on API too, but seed here for demo)
INSERT OR IGNORE INTO wallets(player_id, currency_code, balance)
SELECT id, 'GOLD', 100000 -- 1000.00 GOLD
FROM players WHERE username = 'alice';

INSERT OR IGNORE INTO wallets(player_id, currency_code, balance)
SELECT id, 'GOLD', 50000 -- 500.00 GOLD
FROM players WHERE username = 'bob';

-- Item types
INSERT OR IGNORE INTO item_types(name, rarity, metadata) VALUES
  ('Iron Sword', 'common', '{"atk": 10}'),
  ('Dragon Blade', 'legendary', '{"atk": 100, "fire": true}');

-- Items owned by Alice
INSERT INTO items(type_id, owner_id, status, metadata)
SELECT t.id, p.id, 'in_inventory', '{"durability": 100}'
FROM item_types t, players p
WHERE t.name = 'Iron Sword' AND p.username = 'alice'
  AND NOT EXISTS (
    SELECT 1 FROM items i
    WHERE i.type_id = t.id AND i.owner_id = p.id AND i.metadata = '{"durability": 100}'
  );

INSERT INTO items(type_id, owner_id, status, metadata)
SELECT t.id, p.id, 'in_inventory', '{"durability": 90}'
FROM item_types t, players p
WHERE t.name = 'Dragon Blade' AND p.username = 'alice'
  AND NOT EXISTS (
    SELECT 1 FROM items i
    WHERE i.type_id = t.id AND i.owner_id = p.id AND i.metadata = '{"durability": 90}'
  );
