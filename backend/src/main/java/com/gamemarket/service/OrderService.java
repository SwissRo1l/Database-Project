package com.gamemarket.service;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.entity.PlayerAsset;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.PlayerAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PlayerAssetRepository playerAssetRepository;

    @Transactional
    public void createOrder(Map<String, Object> payload, Integer requesterId) {
        Integer itemId = payload.get("itemId") instanceof Number ? ((Number) payload.get("itemId")).intValue() : Integer.parseInt(payload.get("itemId").toString());
        Integer amount = payload.get("amount") instanceof Number ? ((Number) payload.get("amount")).intValue() : Integer.parseInt(payload.get("amount").toString());
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        String type = payload.get("type").toString().toUpperCase();

        Asset asset = assetRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Asset not found"));

        if ("BUY".equals(type)) {
            BigDecimal total = price.multiply(BigDecimal.valueOf(amount));
            walletService.reserveFunds(requesterId, total);
        } else if ("SELL".equals(type)) {
            PlayerAsset pa = playerAssetRepository.findByPlayerIdAndAsset_AssetId(requesterId, itemId);
            if (pa == null) {
                throw new RuntimeException("Player does not own the item");
            }
            int available = (pa.getQuantity() == null ? 0 : pa.getQuantity()) - (pa.getReservedQuantity() == null ? 0 : pa.getReservedQuantity());
            if (available < amount) {
                throw new RuntimeException("Insufficient item quantity to sell");
            }
            pa.setReservedQuantity(pa.getReservedQuantity() + amount);
            playerAssetRepository.save(pa);
        } else {
            throw new RuntimeException("Unsupported order type: " + type);
        }

        MarketOrder order = new MarketOrder();
        order.setPlayerId(requesterId);
        order.setAsset(asset);
        order.setPrice(price);
        order.setQuantity(amount);
        order.setOrderType(type);
        order.setStatus("OPEN");

        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Integer orderId, Integer requesterId) {
        var opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        MarketOrder order = opt.get();
        if (!order.getPlayerId().equals(requesterId)) {
            throw new RuntimeException("Not the order owner");
        }
        if (!"OPEN".equals(order.getStatus())) {
            throw new RuntimeException("Order cannot be cancelled (status=" + order.getStatus() + ")");
        }

        // Release reserved resources depending on order type
        if ("BUY".equalsIgnoreCase(order.getOrderType())) {
            java.math.BigDecimal total = order.getPrice().multiply(java.math.BigDecimal.valueOf(order.getQuantity()));
            walletService.releaseReserved(requesterId, total);
        } else if ("SELL".equalsIgnoreCase(order.getOrderType())) {
            Integer assetId = order.getAsset().getAssetId();
            PlayerAsset pa = playerAssetRepository.findByPlayerIdAndAsset_AssetId(requesterId, assetId);
            if (pa != null) {
                int reserved = pa.getReservedQuantity() == null ? 0 : pa.getReservedQuantity();
                int toRelease = order.getQuantity() == null ? 0 : order.getQuantity();
                int updated = Math.max(0, reserved - toRelease);
                pa.setReservedQuantity(updated);
                playerAssetRepository.save(pa);
            }
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
}
