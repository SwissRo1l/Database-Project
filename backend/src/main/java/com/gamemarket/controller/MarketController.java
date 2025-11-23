package com.gamemarket.controller;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/listings")
    public List<Map<String, Object>> getListings(
            @RequestParam(required = false) String sort, 
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer itemId) {
        
        List<MarketOrder> orders = orderRepository.findByStatus("OPEN");
        
        if (itemId != null) {
            orders = orders.stream()
                .filter(o -> o.getAsset().getAssetId().equals(itemId))
                .collect(Collectors.toList());
        }
        
        return orders.stream().limit(limit != null ? limit : 50).map(order -> {
            Asset asset = order.getAsset();
            String encodedName = asset.getAssetName().replace(" ", "+");
            String imgUrl = "https://via.placeholder.com/300x200?text=" + encodedName;
            
            return Map.<String, Object>of(
                "id", asset.getAssetId(),
                "name", asset.getAssetName(),
                "price", order.getPrice(),
                "change", (Math.random() * 10) - 5, // Mock change between -5% and +5%
                "img", imgUrl
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return List.of("Rifle", "Sniper", "Pistol", "Knife");
    }
}
