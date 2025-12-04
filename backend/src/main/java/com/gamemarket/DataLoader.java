package com.gamemarket;

import com.gamemarket.entity.*;
import com.gamemarket.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final AssetRepository assetRepository;
    private final WalletRepository walletRepository;
    private final MarketOrderRepository marketOrderRepository;
    private final PlayerAssetRepository playerAssetRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final Random random = new Random();

    public DataLoader(PlayerRepository playerRepository, AssetRepository assetRepository, WalletRepository walletRepository, MarketOrderRepository marketOrderRepository, PlayerAssetRepository playerAssetRepository, TradeHistoryRepository tradeHistoryRepository) {
        this.playerRepository = playerRepository;
        this.assetRepository = assetRepository;
        this.walletRepository = walletRepository;
        this.marketOrderRepository = marketOrderRepository;
        this.playerAssetRepository = playerAssetRepository;
        this.tradeHistoryRepository = tradeHistoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (assetRepository.count() < 10) {
            System.out.println("Seeding database with initial data...");
            seedData();
            System.out.println("Database seeding completed.");
        }
        
        if (playerRepository.count() < 20) {
            System.out.println("Adding more users...");
            seedMoreUsers();
        }

        if (tradeHistoryRepository.count() < 500) {
            System.out.println("Seeding historical trade data for charts...");
            seedHistory();
        }
        
        if (marketOrderRepository.count() < 200) {
            System.out.println("Adding more market orders...");
            seedMoreOrders();
        }

        fixInconsistencies();
    }

    private void fixInconsistencies() {
        System.out.println("Checking for data inconsistencies...");
        List<MarketOrder> orders = marketOrderRepository.findByStatus("OPEN");
        int fixedCount = 0;
        for (MarketOrder order : orders) {
            if ("SELL".equals(order.getOrderType())) {
                PlayerAsset pa = playerAssetRepository.findByPlayerIdAndAsset_AssetId(order.getPlayerId(), order.getAsset().getAssetId());
                if (pa == null) {
                    pa = new PlayerAsset();
                    pa.setPlayerId(order.getPlayerId());
                    pa.setAsset(order.getAsset());
                    pa.setQuantity(order.getQuantity() + 10); // Give them enough + extra
                    pa.setReservedQuantity(order.getQuantity());
                    pa.setPurchaseDate(java.time.LocalDateTime.now());
                    playerAssetRepository.save(pa);
                    fixedCount++;
                } else {
                    // Ensure reserved quantity covers the order
                    int reserved = pa.getReservedQuantity() == null ? 0 : pa.getReservedQuantity();
                    if (reserved < order.getQuantity()) {
                        pa.setReservedQuantity(reserved + order.getQuantity());
                        playerAssetRepository.save(pa);
                        fixedCount++;
                    }
                }
            }
        }
        if (fixedCount > 0) {
            System.out.println("Fixed " + fixedCount + " inconsistent orders.");
        }
    }

    private void seedMoreUsers() {
        String[] extraNames = {"SkyWalker", "DarthVader", "Yoda", "ObiWan", "HanSolo", "Chewbacca", "Leia", "Luke", "R2D2", "C3PO", "Gandalf", "Frodo", "Aragorn", "Legolas", "Gimli", "Sauron", "Gollum", "Bilbo", "Thorin", "Smaug"};
        for (String name : extraNames) {
            if (playerRepository.findByPlayerName(name) == null) {
                Player p = new Player();
                p.setPlayerName(name);
                p.setLevel(random.nextInt(50) + 1);
                playerRepository.save(p);
                
                Wallet w = new Wallet();
                w.setPlayerId(p.getPlayerId());
                w.setBalance(new BigDecimal(random.nextInt(50000) + 5000));
                walletRepository.save(w);
            }
        }
    }

    private void seedHistory() {
        List<Asset> assets = assetRepository.findAll();
        List<Player> players = playerRepository.findAll();
        
        if (players.size() < 2 || assets.isEmpty()) return;

        for (Asset asset : assets) {
            // Generate 30 days of history
            BigDecimal currentPrice = asset.getBasePrice();
            // Start from 30 days ago
            LocalDateTime timeCursor = LocalDateTime.now().minusDays(30);
            
            // Generate ~30-50 trades per asset
            int tradesCount = 30 + random.nextInt(20);
            
            for (int i = 0; i < tradesCount; i++) {
                // Random walk price
                double change = (random.nextDouble() - 0.5) * 0.15; // +/- 7.5% volatility
                currentPrice = currentPrice.add(currentPrice.multiply(BigDecimal.valueOf(change)));
                if (currentPrice.compareTo(BigDecimal.TEN) < 0) currentPrice = BigDecimal.TEN;
                
                // Advance time
                timeCursor = timeCursor.plusHours(12 + random.nextInt(24));
                if (timeCursor.isAfter(LocalDateTime.now())) break;

                Player buyer = players.get(random.nextInt(players.size()));
                Player seller = players.get(random.nextInt(players.size()));
                while (seller.getPlayerId().equals(buyer.getPlayerId())) {
                    seller = players.get(random.nextInt(players.size()));
                }

                int quantity = random.nextInt(5) + 1;

                // Create dummy filled orders to satisfy FK constraints
                MarketOrder buyOrder = new MarketOrder();
                buyOrder.setPlayerId(buyer.getPlayerId());
                buyOrder.setAsset(asset);
                buyOrder.setOrderType("BUY");
                buyOrder.setPrice(currentPrice);
                buyOrder.setQuantity(quantity);
                buyOrder.setStatus("FILLED");
                buyOrder.setCreateTime(timeCursor.minusMinutes(5));
                marketOrderRepository.save(buyOrder);

                MarketOrder sellOrder = new MarketOrder();
                sellOrder.setPlayerId(seller.getPlayerId());
                sellOrder.setAsset(asset);
                sellOrder.setOrderType("SELL");
                sellOrder.setPrice(currentPrice);
                sellOrder.setQuantity(quantity);
                sellOrder.setStatus("FILLED");
                sellOrder.setCreateTime(timeCursor.minusMinutes(10));
                marketOrderRepository.save(sellOrder);

                TradeHistory history = new TradeHistory();
                history.setAsset(asset);
                history.setPrice(currentPrice);
                history.setQuantity(quantity);
                history.setTradeTime(timeCursor);
                history.setBuyOrderId(buyOrder.getOrderId());
                history.setSellOrderId(sellOrder.getOrderId());
                
                tradeHistoryRepository.save(history);
            }
        }
    }

    private void seedMoreOrders() {
        List<Asset> assets = assetRepository.findAll();
        List<Player> players = playerRepository.findAll();
        
        for (int i = 0; i < 150; i++) {
            Player player = players.get(random.nextInt(players.size()));
            Asset asset = assets.get(random.nextInt(assets.size()));
            boolean isSell = random.nextBoolean();
            
            // Price around base price +/- 15%
            BigDecimal price = asset.getBasePrice().multiply(new BigDecimal(0.85 + (random.nextDouble() * 0.3)));
            int quantity = random.nextInt(10) + 1;

            if (isSell) {
                // Ensure player has asset
                PlayerAsset pa = playerAssetRepository.findByPlayerIdAndAsset_AssetId(player.getPlayerId(), asset.getAssetId());
                if (pa == null) {
                    pa = new PlayerAsset();
                    pa.setPlayerId(player.getPlayerId());
                    pa.setAsset(asset);
                    pa.setQuantity(quantity + 5);
                    pa.setReservedQuantity(0);
                    pa.setPurchaseDate(LocalDateTime.now());
                    playerAssetRepository.save(pa);
                } else {
                    pa.setQuantity(pa.getQuantity() + quantity);
                    playerAssetRepository.save(pa);
                }
                
                // Reserve for order
                pa.setReservedQuantity((pa.getReservedQuantity() == null ? 0 : pa.getReservedQuantity()) + quantity);
                playerAssetRepository.save(pa);
                
                createOrder(player.getPlayerId(), asset, "SELL", price, quantity);
            } else {
                // Buy order
                createOrder(player.getPlayerId(), asset, "BUY", price, quantity);
            }
        }
    }

    private void seedData() {
        // Create Players
        List<Player> players = new ArrayList<>();
        String[] playerNames = {"ProGamer123", "NoobMaster69", "TraderJoe", "CryptoKing", "LootGoblin", "SniperWolf", "RushB", "Camper", "Admin", "Whale"};
        
        for (String name : playerNames) {
            Player p = new Player();
            p.setPlayerName(name);
            p.setLevel(random.nextInt(100) + 1);
            players.add(playerRepository.save(p));

            Wallet w = new Wallet();
            w.setPlayerId(p.getPlayerId());
            w.setBalance(new BigDecimal(random.nextInt(10000) + 100));
            walletRepository.save(w);
        }

        // Create Assets (CS:GO Style Skins)
        List<Asset> assets = new ArrayList<>();
        String[] weaponTypes = {"Rifle", "Sniper", "Pistol", "Knife", "SMG", "Shotgun"};
        String[] skinNames = {"Dragon Lore", "Asiimov", "Redline", "Fade", "Hyper Beast", "Neo-Noir", "Vulcan", "Case Hardened", "Doppler", "Howl"};
        String[] weapons = {"AWP", "AK-47", "M4A4", "Desert Eagle", "Karambit", "Butterfly Knife", "USP-S", "Glock-18"};

        for (String weapon : weapons) {
            for (String skin : skinNames) {
                String fullName = weapon + " | " + skin;
                String type = determineType(weapon);
                BigDecimal basePrice = new BigDecimal(random.nextInt(2000) + 50);
                assets.add(createAsset(fullName, type, basePrice));
            }
        }

        // Create Market Orders
        for (int i = 0; i < 100; i++) {
            Player seller = players.get(random.nextInt(players.size()));
            Asset asset = assets.get(random.nextInt(assets.size()));
            
            // Price variation around base price
            BigDecimal price = asset.getBasePrice().multiply(new BigDecimal(0.8 + (random.nextDouble() * 0.4))); // +/- 20%
            
            // Ensure player has the asset before selling
            PlayerAsset pa = new PlayerAsset();
            pa.setPlayerId(seller.getPlayerId());
            pa.setAsset(asset);
            pa.setQuantity(10); // Give them 10
            pa.setReservedQuantity(1); // Reserve 1 for the order
            pa.setPurchaseDate(java.time.LocalDateTime.now());
            playerAssetRepository.save(pa);

            createOrder(seller.getPlayerId(), asset, "SELL", price, 1);
        }
    }

    private String determineType(String weapon) {
        if (weapon.contains("AWP")) return "Sniper";
        if (weapon.contains("AK-47") || weapon.contains("M4A4")) return "Rifle";
        if (weapon.contains("Desert Eagle") || weapon.contains("USP-S") || weapon.contains("Glock-18")) return "Pistol";
        if (weapon.contains("Knife") || weapon.contains("Karambit")) return "Knife";
        return "Rifle";
    }

    private Asset createAsset(String name, String type, BigDecimal price) {
        Asset asset = new Asset();
        asset.setAssetName(name);
        asset.setAssetType(type);
        asset.setBasePrice(price);
        return assetRepository.save(asset);
    }

    private void createOrder(Integer playerId, Asset asset, String type, BigDecimal price, Integer quantity) {
        MarketOrder order = new MarketOrder();
        order.setPlayerId(playerId);
        order.setAsset(asset);
        order.setOrderType(type);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setStatus("OPEN");
        order.setCreateTime(LocalDateTime.now());
        marketOrderRepository.save(order);
    }
}
