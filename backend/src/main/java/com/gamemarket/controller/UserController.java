package com.gamemarket.controller;

import com.gamemarket.entity.MarketOrder;
import com.gamemarket.entity.Player;
import com.gamemarket.entity.PlayerAsset;
import com.gamemarket.entity.Wallet;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.PlayerAssetRepository;
import com.gamemarket.repository.PlayerRepository;
import com.gamemarket.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PlayerAssetRepository playerAssetRepository;

    @Autowired
    private MarketOrderRepository marketOrderRepository;

    @GetMapping("/{id}")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletRepository.findByPlayerId(id);
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal reserved = BigDecimal.ZERO;
        if (wallet != null) {
            balance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
            reserved = wallet.getReserved() == null ? BigDecimal.ZERO : wallet.getReserved();
        }
        // Available funds = Total Balance - Reserved Amount
        BigDecimal available = balance.subtract(reserved);

        return Map.<String, Object>of(
            "username", player.getPlayerName(),
            "email", player.getEmail() != null ? player.getEmail() : "",
            "balance", balance,
            "reserved", reserved,
            "available", available,
            "uid", player.getPlayerId().toString(),
            "avatar", player.getAvatar() != null ? player.getAvatar() : ""
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProfile(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (data.containsKey("username")) {
            player.setPlayerName(data.get("username"));
        }
        if (data.containsKey("password")) {
            String newPassword = data.get("password");
            String currentPassword = data.getOrDefault("currentPassword", "");

            String existingPassword = player.getPassword() == null ? "" : player.getPassword();

            if (!existingPassword.isEmpty() && !existingPassword.equals(currentPassword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
            }

            if (newPassword == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码不能为空");
            }

            player.setPassword(newPassword);
        }
        if (data.containsKey("avatar")) {
            player.setAvatar(data.get("avatar"));
        }
        
        playerRepository.save(player);
        
        return Map.of("message", "Profile updated successfully");
    }

    @PostMapping("/{id}/validate-password")
    public Map<String, Object> validatePassword(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        String currentPassword = data.getOrDefault("currentPassword", "");
        String existingPassword = player.getPassword() == null ? "" : player.getPassword();

        if (!existingPassword.equals(currentPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }

        return Map.of("message", "密码验证通过");
    }

    @GetMapping("/{id}/inventory")
    public List<Map<String, Object>> getInventory(@PathVariable Integer id) {
        List<PlayerAsset> assets = playerAssetRepository.findByPlayerId(id);
        List<MarketOrder> openOrders = marketOrderRepository.findByStatus("OPEN");

        return assets.stream().map(pa -> {
            BigDecimal lowestPrice = openOrders.stream()
                .filter(o -> o.getAsset().getAssetId().equals(pa.getAsset().getAssetId()) && "SELL".equals(o.getOrderType()))
                .map(MarketOrder::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

            return Map.<String, Object>of(
                "id", pa.getAsset().getAssetId(),
                "name", pa.getAsset().getAssetName(),
                "rarity", pa.getAsset().getAssetType(),
                "img", "https://via.placeholder.com/150?text=" + pa.getAsset().getAssetName().replace(" ", "+"),
                "price", lowestPrice != null ? lowestPrice : "暂无报价",
                "purchaseDate", pa.getPurchaseDate() != null ? pa.getPurchaseDate().toString() : "未知",
                "quantity", pa.getQuantity(),
                "reserved", pa.getReservedQuantity() != null ? pa.getReservedQuantity() : 0
            );
        }).collect(Collectors.toList());
    }
}
