package com.gamemarket.controller;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private com.gamemarket.repository.TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping("/listings")
    public Object getListings(
            @RequestParam(required = false) String sort, 
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        List<MarketOrder> orders = orderRepository.findByStatus("OPEN");
        
        if (itemId != null) {
            orders = orders.stream()
                .filter(o -> o.getAsset().getAssetId().equals(itemId))
                .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            orders = orders.stream()
                .filter(o -> o.getAsset().getAssetName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
        }

        if (category != null && !category.isEmpty() && !category.equals("All")) {
            orders = orders.stream()
                .filter(o -> o.getAsset() != null && o.getAsset().getAssetType() != null && o.getAsset().getAssetType().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }

        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    orders.sort((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()));
                    break;
                case "price_desc":
                    orders.sort((o1, o2) -> o2.getPrice().compareTo(o1.getPrice()));
                    break;
                case "newest":
                    orders.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
                    break;
                // 'hot' and 'gainers' are currently just placeholders or default sort
            }
        }

        // Pagination Logic
        if (page != null && size != null) {
            int totalElements = orders.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            List<MarketOrder> pagedOrders;
            if (start >= totalElements) {
                pagedOrders = List.of();
            } else {
                pagedOrders = orders.subList(start, end);
            }

            List<Map<String, Object>> content = pagedOrders.stream().map(this::mapOrderToResponse).collect(Collectors.toList());

            return Map.of(
                "content", content,
                "totalPages", totalPages,
                "totalElements", totalElements,
                "number", page,
                "size", size
            );
        }
        
        return orders.stream().limit(limit != null ? limit : 100).map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    private Map<String, Object> mapOrderToResponse(MarketOrder order) {
        Asset asset = order.getAsset();
        String encodedName = asset.getAssetName().replace(" ", "+");
        String imgUrl = "https://via.placeholder.com/300x200?text=" + encodedName;
        
        return Map.<String, Object>of(
            "id", asset.getAssetId(),
            "orderId", order.getOrderId(),
            "name", asset.getAssetName(),
            "price", order.getPrice(),
            "quantity", order.getQuantity(),
            "type", order.getOrderType(),
            "createTime", order.getCreateTime(),
            "change", (Math.random() * 10) - 5, // Mock change between -5% and +5%
            "img", imgUrl
        );
    }

    @PostMapping("/trade")
    public ResponseEntity<?> executeTrade(@RequestBody Map<String, Object> payload) {
        try {
            Integer orderId = Integer.parseInt(payload.get("orderId").toString());
            Integer userId = Integer.parseInt(payload.get("userId").toString());
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());
            
            orderService.executeTrade(orderId, userId, quantity);
            return ResponseEntity.ok(Map.of("message", "Trade executed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return List.of("Rifle", "Sniper", "Pistol", "Knife");
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getTradeHistory(@RequestParam Integer itemId) {
        return tradeHistoryRepository.findByAsset_AssetIdOrderByTradeTimeAsc(itemId).stream()
            .map(h -> Map.<String, Object>of(
                "time", h.getTradeTime(),
                "price", h.getPrice(),
                "quantity", h.getQuantity()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/history/daily")
    public List<Map<String, Object>> getDailyHistory(@RequestParam Integer itemId) {
        List<Object[]> results = tradeHistoryRepository.findDailyMinPriceByAssetId(itemId);
        return results.stream()
            .map(row -> Map.<String, Object>of(
                "date", row[0].toString(),
                "price", row[1]
            ))
            .collect(Collectors.toList());
    }
}
