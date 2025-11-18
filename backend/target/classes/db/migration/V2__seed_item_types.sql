INSERT INTO item_types(name, rarity, metadata) VALUES
  ('Iron Sword', 'common', '{"atk": 10}')
ON CONFLICT (name) DO NOTHING;

INSERT INTO item_types(name, rarity, metadata) VALUES
  ('Dragon Blade', 'legendary', '{"atk": 100, "fire": true}')
ON CONFLICT (name) DO NOTHING;
