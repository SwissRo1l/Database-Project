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

    @PostMapping("/orders")
    public Map<String, String> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            orderService.createOrder(payload, 1);
            return Map.of("message", "Order created successfully");
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
