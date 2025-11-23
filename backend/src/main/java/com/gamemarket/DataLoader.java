package com.gamemarket;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.Player;
import com.gamemarket.entity.Wallet;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.PlayerRepository;
import com.gamemarket.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final AssetRepository assetRepository;
    private final WalletRepository walletRepository;
    private final MarketOrderRepository marketOrderRepository;
    private final Random random = new Random();

    public DataLoader(PlayerRepository playerRepository, AssetRepository assetRepository, WalletRepository walletRepository, MarketOrderRepository marketOrderRepository) {
        this.playerRepository = playerRepository;
        this.assetRepository = assetRepository;
        this.walletRepository = walletRepository;
        this.marketOrderRepository = marketOrderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (assetRepository.count() < 10) {
            System.out.println("Seeding database with initial data...");
            seedData();
            System.out.println("Database seeding completed.");
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
        marketOrderRepository.save(order);
    }
}
