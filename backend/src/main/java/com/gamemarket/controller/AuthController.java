package com.gamemarket.controller;

import com.gamemarket.entity.Player;
import com.gamemarket.entity.Wallet;
import com.gamemarket.repository.PlayerRepository;
import com.gamemarket.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private WalletRepository walletRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String usernameOrEmail = credentials.get("username");
        String password = credentials.get("password");

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username or email is required"));
        }

        Player player = playerRepository.findByPlayerName(usernameOrEmail);
        if (player == null) {
            player = playerRepository.findByEmail(usernameOrEmail);
        }

        if (player != null && (player.getPassword() == null || player.getPassword().equals(password))) {
            return ResponseEntity.ok(Map.of(
                "token", "mock-token-" + player.getPlayerId(),
                "userId", player.getPlayerId(),
                "username", player.getPlayerName(),
                "email", player.getEmail() != null ? player.getEmail() : ""
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        String email = data.get("email");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }

        if (playerRepository.findByPlayerName(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already exists"));
        }

        Player player = new Player();
        player.setPlayerName(username);
        player.setPassword(password);
        player.setEmail(email);
        player.setLevel(1);
        
        Player savedPlayer = playerRepository.save(player);
        
        // Create initial wallet
        Wallet wallet = new Wallet();
        wallet.setPlayerId(savedPlayer.getPlayerId());
        wallet.setBalance(new BigDecimal("1000.00"));
        walletRepository.save(wallet);

        return ResponseEntity.ok(Map.of(
            "token", "mock-token-" + savedPlayer.getPlayerId(),
            "userId", savedPlayer.getPlayerId(),
            "username", savedPlayer.getPlayerName(),
            "email", savedPlayer.getEmail() != null ? savedPlayer.getEmail() : ""
        ));
    }
}
