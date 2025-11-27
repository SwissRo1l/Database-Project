package com.gamemarket.controller;

import com.gamemarket.entity.MarketOrder;
import com.gamemarket.entity.TradeHistory;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.TradeHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trade")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private com.gamemarket.service.OrderService orderService;

    @Autowired
    private com.gamemarket.repository.WalletRepository walletRepository;

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            // Determine requester id from payload if provided, otherwise default to 1
            Integer requesterId = 1;
            if (payload.containsKey("userId") && payload.get("userId") != null) {
                Object uidObj = payload.get("userId");
                if (uidObj instanceof Number) {
                    requesterId = ((Number) uidObj).intValue();
                } else {
                    requesterId = Integer.parseInt(uidObj.toString());
                }
            }

            var trades = orderService.createOrder(payload, requesterId);

            // Fetch updated wallet for requester to provide immediate balance feedback
            var wallet = walletRepository.findByPlayerId(requesterId);
            java.math.BigDecimal balance = java.math.BigDecimal.ZERO;
            java.math.BigDecimal reserved = java.math.BigDecimal.ZERO;
            if (wallet != null) {
                balance = wallet.getBalance() == null ? java.math.BigDecimal.ZERO : wallet.getBalance();
                reserved = wallet.getReserved() == null ? java.math.BigDecimal.ZERO : wallet.getReserved();
            }
            // After reservation we move funds out of balance into reserved, so 'balance' already represents available funds
            java.math.BigDecimal available = balance;

            return Map.of(
                "message", "Order created successfully",
                "trades", trades,
                "balance", balance,
                "reserved", reserved,
                "available", available
            );
        } catch (RuntimeException ex) {
            return Map.of("message", "Order creation failed: " + ex.getMessage());
        }
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getOrders(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            // Return trade history for profile
            List<TradeHistory> history = tradeHistoryRepository.findByPlayerId(userId);
            return history.stream().map(h -> Map.<String, Object>of(
                "id", h.getTradeId(),
                "type", "buy", // Simplified
                "itemName", h.getAsset().getAssetName(),
                "date", h.getTradeTime().toString(),
                "price", h.getPrice(),
                "amount", h.getQuantity()
            )).collect(Collectors.toList());
        }
        return List.of();
    }
    @DeleteMapping("/orders/{orderId}")
    public Map<String, String> cancelOrder(@PathVariable Integer orderId) {
        try {
            orderService.cancelOrder(orderId, 1);
            return Map.of("message", "Order cancelled successfully");
        } catch (RuntimeException ex) {
            return Map.of("message", "Cancel failed: " + ex.getMessage());
        }
    }
}
