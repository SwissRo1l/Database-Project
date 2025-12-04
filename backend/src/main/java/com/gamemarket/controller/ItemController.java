package com.gamemarket.controller;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.TradeHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @GetMapping("/{id}")
    public Map<String, Object> getItem(@PathVariable Integer id) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        List<MarketOrder> openOrders = orderRepository.findByAsset_AssetIdAndStatus(id, "OPEN");

        Optional<BigDecimal> lowestPrice = openOrders.stream()
            .map(MarketOrder::getPrice)
            .min(BigDecimal::compareTo);

        BigDecimal displayPrice = lowestPrice.orElseGet(() -> Optional.ofNullable(asset.getBasePrice()).orElse(BigDecimal.ZERO));

        Instant now = Instant.now();
        Instant dayBefore = now.minus(Duration.ofHours(24));
        Timestamp sinceTs = Timestamp.from(dayBefore);
        Timestamp beforeTs = Timestamp.from(dayBefore);

        Long sales24 = Optional.ofNullable(tradeHistoryRepository.sumQuantitySince(id, sinceTs)).orElse(0L);
        Double latestPrice = tradeHistoryRepository.findLatestPriceByAssetId(id);
        Double price24hAgo = tradeHistoryRepository.findPriceAtOrBefore(id, beforeTs);
        double changePercent = 0.0;
        if (latestPrice != null && price24hAgo != null && price24hAgo != 0.0) {
            changePercent = ((latestPrice - price24hAgo) / price24hAgo) * 100.0;
        }

        String encodedName = asset.getAssetName().replace(" ", "+");
        String imgUrl = "https://via.placeholder.com/400x300?text=" + encodedName;

        return Map.<String, Object>of(
            "id", asset.getAssetId(),
            "name", asset.getAssetName(),
            "price", displayPrice,
            "change", changePercent,
            "sales24", sales24,
            "img", imgUrl,
            "description", "Type: " + asset.getAssetType()
        );
    }

    @GetMapping("/search")
    public List<Asset> searchItems(@RequestParam String q) {
        // Simplified search
        return assetRepository.findAll(); 
    }
}
