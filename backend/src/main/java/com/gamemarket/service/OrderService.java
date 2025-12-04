package com.gamemarket.service;

import com.gamemarket.entity.*;
import com.gamemarket.repository.*;
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

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

        // Notify User
        Notification n = new Notification();
        n.setUserId(requesterId);
        n.setMessage("您的挂单 (" + order.getAsset().getAssetName() + ") 已成功取消");
        notificationRepository.save(n);
    }

    @Transactional
    public void executeTrade(Integer orderId, Integer executorId, Integer quantity) {
        MarketOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getPlayerId().equals(executorId)) {
            throw new RuntimeException("Cannot trade with yourself");
        }
        if (!"OPEN".equals(order.getStatus())) {
            throw new RuntimeException("Order is not open");
        }
        if (order.getQuantity() < quantity) {
            throw new RuntimeException("Not enough quantity in order");
        }

        BigDecimal totalPrice = order.getPrice().multiply(BigDecimal.valueOf(quantity));
        Integer assetId = order.getAsset().getAssetId();

        MarketOrder counterOrder = new MarketOrder();
        counterOrder.setPlayerId(executorId);
        counterOrder.setAsset(order.getAsset());
        counterOrder.setPrice(order.getPrice());
        counterOrder.setQuantity(quantity);
        counterOrder.setStatus("FILLED");
        counterOrder.setCreateTime(java.time.LocalDateTime.now());

        if ("SELL".equals(order.getOrderType())) {
            // Executor is BUYING
            counterOrder.setOrderType("BUY");
            
            walletService.deductFunds(executorId, totalPrice);
            walletService.addFunds(order.getPlayerId(), totalPrice);
            
            PlayerAsset sellerAsset = playerAssetRepository.findByPlayerIdAndAsset_AssetId(order.getPlayerId(), assetId);
            if (sellerAsset == null) {
                throw new RuntimeException("Seller asset not found (Data inconsistency)");
            }
            int reserved = sellerAsset.getReservedQuantity() == null ? 0 : sellerAsset.getReservedQuantity();
            sellerAsset.setReservedQuantity(Math.max(0, reserved - quantity));
            sellerAsset.setQuantity(sellerAsset.getQuantity() - quantity);
            playerAssetRepository.save(sellerAsset);
            
            PlayerAsset executorAsset = playerAssetRepository.findByPlayerIdAndAsset_AssetId(executorId, assetId);
            if (executorAsset == null) {
                executorAsset = new PlayerAsset();
                executorAsset.setPlayerId(executorId);
                executorAsset.setAsset(order.getAsset());
                executorAsset.setQuantity(0);
                executorAsset.setReservedQuantity(0);
            }
            executorAsset.setQuantity(executorAsset.getQuantity() + quantity);
            executorAsset.setPurchaseDate(java.time.LocalDateTime.now());
            playerAssetRepository.save(executorAsset);

            // Notify Seller
            Notification n = new Notification();
            n.setUserId(order.getPlayerId());
            n.setMessage("您的商品 (" + order.getAsset().getAssetName() + ") 已被购买 " + quantity + " 个，获得 " + totalPrice + " G");
            notificationRepository.save(n);

        } else {
            // Executor is SELLING
            counterOrder.setOrderType("SELL");

            PlayerAsset executorAsset = playerAssetRepository.findByPlayerIdAndAsset_AssetId(executorId, assetId);
            int available = (executorAsset == null ? 0 : executorAsset.getQuantity()) - (executorAsset == null ? 0 : executorAsset.getReservedQuantity());
            if (available < quantity) {
                throw new RuntimeException("Insufficient items to sell");
            }
            
            executorAsset.setQuantity(executorAsset.getQuantity() - quantity);
            playerAssetRepository.save(executorAsset);
            
            PlayerAsset buyerAsset = playerAssetRepository.findByPlayerIdAndAsset_AssetId(order.getPlayerId(), assetId);
            if (buyerAsset == null) {
                buyerAsset = new PlayerAsset();
                buyerAsset.setPlayerId(order.getPlayerId());
                buyerAsset.setAsset(order.getAsset());
                buyerAsset.setQuantity(0);
                buyerAsset.setReservedQuantity(0);
            }
            buyerAsset.setQuantity(buyerAsset.getQuantity() + quantity);
            buyerAsset.setPurchaseDate(java.time.LocalDateTime.now());
            playerAssetRepository.save(buyerAsset);
            
            walletService.commitReserved(order.getPlayerId(), totalPrice);
            walletService.addFunds(executorId, totalPrice);

            // Notify Buyer (Maker)
            Notification n = new Notification();
            n.setUserId(order.getPlayerId());
            n.setMessage("您的求购 (" + order.getAsset().getAssetName() + ") 已成交 " + quantity + " 个，花费 " + totalPrice + " G");
            notificationRepository.save(n);
        }

        orderRepository.save(counterOrder);

        order.setQuantity(order.getQuantity() - quantity);
        if (order.getQuantity() == 0) {
            order.setStatus("FILLED");
        }
        orderRepository.save(order);

        TradeHistory history = new TradeHistory();
        history.setAsset(order.getAsset());
        history.setPrice(order.getPrice());
        history.setQuantity(quantity);
        history.setTradeTime(java.time.LocalDateTime.now());
        if ("SELL".equals(order.getOrderType())) {
            history.setSellOrderId(order.getOrderId());
            history.setBuyOrderId(counterOrder.getOrderId());
        } else {
            history.setBuyOrderId(order.getOrderId());
            history.setSellOrderId(counterOrder.getOrderId());
        }
        tradeHistoryRepository.save(history);
    }
}
