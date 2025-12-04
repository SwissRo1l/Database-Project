package com.gamemarket.controller;

import com.gamemarket.entity.Wallet;
import com.gamemarket.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/{playerId}/recharge")
    public Map<String, Object> recharge(@PathVariable Integer playerId, @RequestBody RechargeRequest req) {
        Wallet w = walletService.recharge(playerId, req.getAmount());
        return Map.of(
            "message", "Recharge successful",
            "playerId", playerId,
            "balance", w.getBalance()
        );
    }
}
