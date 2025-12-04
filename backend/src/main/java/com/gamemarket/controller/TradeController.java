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

            orderService.createOrder(payload, requesterId);

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
                "balance", balance,
                "reserved", reserved,
                "available", available
            );
        } catch (RuntimeException ex) {
            return Map.of("message", "Order creation failed: " + ex.getMessage());
        }
    }

    @GetMapping("/orders")
    public Object getOrders(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (userId != null) {
            List<TradeHistory> history;
            long totalElements = 0;
            int totalPages = 0;

            if (page != null && size != null) {
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("tradeTime").descending());
                org.springframework.data.domain.Page<TradeHistory> historyPage = tradeHistoryRepository.findByPlayerId(userId, pageable);
                history = historyPage.getContent();
                totalElements = historyPage.getTotalElements();
                totalPages = historyPage.getTotalPages();
            } else {
                history = tradeHistoryRepository.findByPlayerId(userId);
                totalElements = history.size();
                totalPages = 1;
            }
            
            // Collect all order IDs to fetch ownership info
            java.util.Set<Integer> orderIds = new java.util.HashSet<>();
            for (TradeHistory h : history) {
                orderIds.add(h.getBuyOrderId());
                orderIds.add(h.getSellOrderId());
            }
            
            // Fetch all related orders to determine if user was buyer or seller
            List<MarketOrder> orders = orderRepository.findAllById(orderIds);
            Map<Integer, Integer> orderOwnerMap = orders.stream()
                .collect(Collectors.toMap(MarketOrder::getOrderId, MarketOrder::getPlayerId));

            List<Map<String, Object>> content = history.stream().map(h -> {
                String type = "unknown";
                Integer buyerId = orderOwnerMap.get(h.getBuyOrderId());
                Integer sellerId = orderOwnerMap.get(h.getSellOrderId());
                
                if (buyerId != null && buyerId.equals(userId)) {
                    type = "buy";
                } else if (sellerId != null && sellerId.equals(userId)) {
                    type = "sell";
                }
                
                return Map.<String, Object>of(
                    "id", h.getTradeId(),
                    "type", type,
                    "itemName", h.getAsset().getAssetName(),
                    "date", h.getTradeTime().toString(),
                    "price", h.getPrice(),
                    "amount", h.getQuantity()
                );
            }).collect(Collectors.toList());

            if (page != null && size != null) {
                return Map.of(
                    "content", content,
                    "totalElements", totalElements,
                    "totalPages", totalPages,
                    "number", page,
                    "size", size
                );
            } else {
                return content;
            }
        }
        return List.of();
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingOrders(@RequestParam Integer userId) {
        return orderRepository.findByPlayerIdAndStatus(userId, "OPEN").stream()
            .map(o -> Map.<String, Object>of(
                "orderId", o.getOrderId(),
                "assetId", o.getAsset().getAssetId(),
                "assetName", o.getAsset().getAssetName(),
                "price", o.getPrice(),
                "quantity", o.getQuantity(),
                "type", o.getOrderType(),
                "createTime", o.getCreateTime()
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(@RequestBody Map<String, Object> payload) {
        try {
            Integer orderId = Integer.parseInt(payload.get("orderId").toString());
            Integer userId = Integer.parseInt(payload.get("userId").toString());
            orderService.cancelOrder(orderId, userId);
            return Map.of("message", "Order cancelled successfully");
        } catch (Exception e) {
            return Map.of("message", "Cancel failed: " + e.getMessage());
        }
    }
}
