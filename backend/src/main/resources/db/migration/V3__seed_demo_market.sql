-- Seed demo players, wallets, items, and listings (idempotent)

-- players
INSERT INTO players(username) VALUES ('luna') ON CONFLICT (username) DO NOTHING;
INSERT INTO players(username) VALUES ('milo') ON CONFLICT (username) DO NOTHING;

-- wallets (GOLD)
INSERT INTO wallets(player_id, currency_code, balance)
SELECT p.id, 'GOLD', 200000 FROM players p WHERE p.username='luna'
ON CONFLICT (player_id, currency_code) DO NOTHING;

INSERT INTO wallets(player_id, currency_code, balance)
SELECT p.id, 'GOLD', 120000 FROM players p WHERE p.username='milo'
ON CONFLICT (player_id, currency_code) DO NOTHING;

-- items for luna
INSERT INTO items(type_id, owner_id, status, metadata)
SELECT t.id, p.id, 'in_inventory', '{"durability": 90}'
FROM item_types t, players p
WHERE t.name='Iron Sword' AND p.username='luna'
  AND NOT EXISTS (
    SELECT 1 FROM items i WHERE i.owner_id=p.id AND i.type_id=t.id AND i.status='in_inventory'
  );

-- create listing if luna has an inventory item and no listing for it
INSERT INTO listings(item_id, seller_id, currency_code, price, status)
SELECT i.id, i.owner_id, 'GOLD', 1800, 'active'
FROM items i
JOIN players p ON p.id=i.owner_id AND p.username='luna'
LEFT JOIN listings l ON l.item_id = i.id
WHERE i.status='in_inventory' AND l.id IS NULL
LIMIT 1;
