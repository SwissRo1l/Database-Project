package com.example.market.web;

import com.example.market.domain.Wallet;
import com.example.market.service.WalletService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private record DepositRequest(@NotBlank String username, @NotBlank String currency, @Min(1) long amount) {}

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest body) {
        try {
            Wallet w = service.deposit(body.username(), body.currency(), body.amount());
            return ResponseEntity.ok(Map.of(
                "username", w.getPlayer().getUsername(),
                "currency", w.getCurrency().getCode(),
                "balance", w.getBalance()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> wallets(@PathVariable String username) {
        try {
            var list = service.listWallets(username);
            return ResponseEntity.ok(
                list.stream().map(w -> Map.of(
                    "currency", w.getCurrency().getCode(),
                    "balance", w.getBalance()
                )).toList()
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
