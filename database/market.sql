CREATE TABLE Player (
    player_id SERIAL PRIMARY KEY,
    player_name VARCHAR(50) UNIQUE NOT NULL,
    level INT DEFAULT 1,
    register_time TIMESTAMP DEFAULT NOW()
);
CREATE TABLE Wallet (
    wallet_id SERIAL PRIMARY KEY,
    player_id INT NOT NULL,
    balance DECIMAL(18,2) DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES Player(player_id)
);
CREATE TABLE Asset (
    asset_id SERIAL PRIMARY KEY,
    asset_name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    base_price DECIMAL(18,2)
);
CREATE TABLE PlayerAsset (
    id SERIAL PRIMARY KEY,
    player_id INT NOT NULL,
    asset_id INT NOT NULL,
    quantity INT DEFAULT 1,

    FOREIGN KEY (player_id) REFERENCES Player(player_id),
    FOREIGN KEY (asset_id) REFERENCES Asset(asset_id)
);
CREATE TABLE MarketOrder (
    order_id SERIAL PRIMARY KEY,
    player_id INT NOT NULL,
    asset_id INT NOT NULL,
    order_type VARCHAR(4) NOT NULL CHECK (order_type IN ('BUY','SELL')),
    price DECIMAL(18,2) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',

    create_time TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (player_id) REFERENCES Player(player_id),
    FOREIGN KEY (asset_id) REFERENCES Asset(asset_id)
);
CREATE TABLE TradeHistory (
    trade_id SERIAL PRIMARY KEY,
    buy_order_id INT NOT NULL,
    sell_order_id INT NOT NULL,
    asset_id INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    quantity INT NOT NULL,
    trade_time TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (buy_order_id) REFERENCES MarketOrder(order_id),
    FOREIGN KEY (sell_order_id) REFERENCES MarketOrder(order_id),
    FOREIGN KEY (asset_id) REFERENCES Asset(asset_id)
);
CREATE TABLE MarketPriceIndex (
    id SERIAL PRIMARY KEY,
    asset_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (asset_id) REFERENCES Asset(asset_id)
);
