package com.gamemarket.controller;

import com.gamemarket.entity.Player;
import com.gamemarket.entity.PlayerAsset;
import com.gamemarket.entity.Wallet;
import com.gamemarket.repository.PlayerAssetRepository;
import com.gamemarket.repository.PlayerRepository;
import com.gamemarket.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletRepository.findByPlayerId(id);
        
        return Map.<String, Object>of(
            "username", player.getPlayerName(),
            "email", player.getEmail() != null ? player.getEmail() : "",
            "balance", wallet != null ? wallet.getBalance() : BigDecimal.ZERO,
            "uid", player.getPlayerId().toString()
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProfile(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (data.containsKey("username")) {
            player.setPlayerName(data.get("username"));
        }
        if (data.containsKey("password")) {
            player.setPassword(data.get("password"));
        }
        
        playerRepository.save(player);
        
        return Map.of("message", "Profile updated successfully");
    }

    @GetMapping("/{id}/inventory")
    public List<Map<String, Object>> getInventory(@PathVariable Integer id) {
        List<PlayerAsset> assets = playerAssetRepository.findByPlayerId(id);
        return assets.stream().map(pa -> Map.<String, Object>of(
            "id", pa.getAsset().getAssetId(),
            "name", pa.getAsset().getAssetName(),
            "rarity", "Common", // Mock
            "img", "https://via.placeholder.com/150",
            "price", pa.getAsset().getBasePrice()
        )).collect(Collectors.toList());
    }
}
