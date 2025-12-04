package com.gamemarket.controller;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // If itemId specified, return individual orders for that asset (existing behavior)
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
                // 'hot' and 'gainers' are handled below when itemId is not provided
            }
        }

        // If itemId provided, return orders as before
        if (itemId != null) {
            // Pagination for orders
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

        // No itemId: aggregate by asset to build market listing summary (one entry per asset)
        Map<Integer, List<MarketOrder>> ordersByAsset = orders.stream()
            .filter(o -> o.getAsset() != null)
            .collect(Collectors.groupingBy(o -> o.getAsset().getAssetId()));

        List<Map<String, Object>> assetSummaries = ordersByAsset.entrySet().stream().map(entry -> {
            Integer assetIdKey = entry.getKey();
            List<MarketOrder> assetOrders = entry.getValue();
            Asset asset = assetOrders.get(0).getAsset();

            // lowest sell price
            Optional<java.math.BigDecimal> lowestPrice = assetOrders.stream().map(MarketOrder::getPrice).min(java.util.Comparator.naturalOrder());

            // compute sales in last 24 hours and change percent using DB-side aggregations for performance
            java.time.Instant now = java.time.Instant.now();
            java.time.Instant dayBefore = now.minus(Duration.ofHours(24));
            java.sql.Timestamp sinceTs = java.sql.Timestamp.from(dayBefore);
            java.sql.Timestamp beforeTs = java.sql.Timestamp.from(dayBefore);

            Long sales24 = 0L;
            try {
                sales24 = Optional.ofNullable(tradeHistoryRepository.sumQuantitySince(assetIdKey, sinceTs)).orElse(0L);
            } catch (Exception ex) {
                // fallback to 0 on error
                sales24 = 0L;
            }

            Double latestPrice = Optional.ofNullable(tradeHistoryRepository.findLatestPriceByAssetId(assetIdKey)).orElse(null);
            Double price24hAgo = Optional.ofNullable(tradeHistoryRepository.findPriceAtOrBefore(assetIdKey, beforeTs)).orElse(null);
            Double changePercent = 0.0;
            if (latestPrice != null && price24hAgo != null && price24hAgo != 0) {
                changePercent = ((latestPrice - price24hAgo) / price24hAgo) * 100.0;
            }

            String encodedName = asset.getAssetName().replace(" ", "+");
            String imgUrl = "https://via.placeholder.com/300x200?text=" + encodedName;

            return Map.<String, Object>of(
                "id", asset.getAssetId(),
                "name", asset.getAssetName(),
                "price", lowestPrice.orElse(java.math.BigDecimal.ZERO),
                "img", imgUrl,
                "sales24", sales24,
                "change", changePercent,
                "hasSellOrders", true
            );
        }).collect(Collectors.toList());

        // Sort asset summaries according to sort param
        if (sort != null) {
            switch (sort) {
                case "hot":
                    assetSummaries.sort((a, b) -> Long.compare((Long) b.getOrDefault("sales24", 0L), (Long) a.getOrDefault("sales24", 0L)));
                    break;
                case "gainers":
                    assetSummaries.sort((a, b) -> Double.compare(((Number) b.getOrDefault("change", 0)).doubleValue(), ((Number) a.getOrDefault("change", 0)).doubleValue()));
                    break;
                case "price_asc":
                    assetSummaries.sort((a, b) -> new java.math.BigDecimal(a.get("price" ).toString()).compareTo(new java.math.BigDecimal(b.get("price").toString())));
                    break;
                case "price_desc":
                    assetSummaries.sort((a, b) -> new java.math.BigDecimal(b.get("price" ).toString()).compareTo(new java.math.BigDecimal(a.get("price").toString())));
                    break;
                default:
                    break;
            }
        }

        // Pagination for asset summaries
        if (page != null && size != null) {
            int totalElements = assetSummaries.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);

            List<Map<String, Object>> pagedAssets;
            if (start >= totalElements) {
                pagedAssets = List.of();
            } else {
                pagedAssets = assetSummaries.subList(start, end);
            }

            return Map.of(
                "content", pagedAssets,
                "totalPages", totalPages,
                "totalElements", totalElements,
                "number", page,
                "size", size
            );
        }

        return assetSummaries.stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
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

    @GetMapping("/history/hourly")
    public List<Map<String, Object>> getHourlyHistory(
            @RequestParam Integer itemId,
            @RequestParam(required = false) Integer hours) {
        int rangeHours = (hours != null && hours > 0) ? hours : 24;
        java.time.Instant now = java.time.Instant.now();
        java.sql.Timestamp since = java.sql.Timestamp.from(now.minus(java.time.Duration.ofHours(rangeHours)));
        List<Object[]> results = tradeHistoryRepository.findHourlyAvgPriceByAssetIdSince(itemId, since);
        return results.stream()
            .map(row -> {
                java.sql.Timestamp ts = row[0] instanceof java.sql.Timestamp ? (java.sql.Timestamp) row[0] : java.sql.Timestamp.valueOf(row[0].toString());
                return Map.<String, Object>of(
                    "time", ts.toInstant().toString(),
                    "price", row[1]
                );
            })
            .collect(Collectors.toList());
    }
}
