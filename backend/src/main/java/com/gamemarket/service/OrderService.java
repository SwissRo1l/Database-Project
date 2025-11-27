package com.gamemarket.service;

import com.gamemarket.entity.Asset;
import com.gamemarket.entity.MarketOrder;
import com.gamemarket.entity.PlayerAsset;
import com.gamemarket.entity.TradeHistory;
import com.gamemarket.repository.AssetRepository;
import com.gamemarket.repository.MarketOrderRepository;
import com.gamemarket.repository.PlayerAssetRepository;
import com.gamemarket.repository.TradeHistoryRepository;
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

    @Transactional
    public java.util.List<TradeHistory> createOrder(Map<String, Object> payload, Integer requesterId) {
        Integer itemId = payload.get("itemId") instanceof Number ? ((Number) payload.get("itemId")).intValue() : Integer.parseInt(payload.get("itemId").toString());
        Integer amount = payload.get("amount") instanceof Number ? ((Number) payload.get("amount")).intValue() : Integer.parseInt(payload.get("amount").toString());
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        String type = payload.get("type").toString().toUpperCase();

        Asset asset = assetRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Asset not found"));

        if ("BUY".equals(type)) {
            // No reservation: balance will be deducted at execution time (debit)
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

        // Attempt immediate matching
        int remaining = order.getQuantity() == null ? 0 : order.getQuantity();
        java.util.List<TradeHistory> executedTrades = new java.util.ArrayList<>();
        // No reservation model: we debit buyers at execution time, so no need to track reserved vs actual for refund

        // Get open orders and try match
        java.util.List<MarketOrder> openOrders = orderRepository.findByStatus("OPEN");

        // Filter candidate orders
        java.util.stream.Stream<MarketOrder> candidates = openOrders.stream()
            .filter(o -> o.getAsset().getAssetId().equals(itemId))
            .filter(o -> !o.getPlayerId().equals(requesterId))
            .filter(o -> !o.getOrderId().equals(order.getOrderId()))
            .filter(o -> !o.getOrderType().equalsIgnoreCase(order.getOrderType()));

        if ("BUY".equalsIgnoreCase(type)) {
            // For buy order, match cheapest sell orders first with price <= buy price
            candidates = candidates.filter(o -> o.getPrice().compareTo(price) <= 0)
                .sorted((a, b) -> {
                    int cmp = a.getPrice().compareTo(b.getPrice());
                    if (cmp != 0) return cmp;
                    return a.getCreateTime().compareTo(b.getCreateTime());
                });
        } else {
            // For sell order, match highest buy orders first with price >= sell price
            candidates = candidates.filter(o -> o.getPrice().compareTo(price) >= 0)
                .sorted((a, b) -> {
                    int cmp = b.getPrice().compareTo(a.getPrice());
                    if (cmp != 0) return cmp;
                    return a.getCreateTime().compareTo(b.getCreateTime());
                });
        }

        for (MarketOrder match : (java.util.List<MarketOrder>) candidates.toList()) {
            if (remaining <= 0) break;
            int matchQty = match.getQuantity() == null ? 0 : match.getQuantity();
            if (matchQty <= 0) continue;
            int tradeQty = Math.min(remaining, matchQty);
            BigDecimal tradePrice = match.getPrice(); // use maker price

            Integer buyOrderId = null;
            Integer sellOrderId = null;
            Integer buyerId = null;
            Integer sellerId = null;

            if ("BUY".equalsIgnoreCase(type)) {
                buyerId = requesterId;
                sellerId = match.getPlayerId();
                buyOrderId = order.getOrderId();
                sellOrderId = match.getOrderId();

                // Commit buyer funds and credit seller
                BigDecimal total = tradePrice.multiply(BigDecimal.valueOf(tradeQty));
                // Ensure buyer has funds, then directly debit and credit seller
                if (!walletService.hasSufficientFunds(buyerId, total)) {
                    // Skip this match if buyer cannot pay
                    continue;
                }
                walletService.debit(buyerId, total);
                walletService.credit(sellerId, total);
                // buyer was debited directly for this trade

                // Transfer assets: decrease seller reserved and quantity
                PlayerAsset sellerPA = playerAssetRepository.findByPlayerIdAndAsset_AssetId(sellerId, itemId);
                if (sellerPA != null) {
                    int reserved = sellerPA.getReservedQuantity() == null ? 0 : sellerPA.getReservedQuantity();
                    int q = sellerPA.getQuantity() == null ? 0 : sellerPA.getQuantity();
                    sellerPA.setReservedQuantity(Math.max(0, reserved - tradeQty));
                    sellerPA.setQuantity(Math.max(0, q - tradeQty));
                    playerAssetRepository.save(sellerPA);
                }

                // Give assets to buyer
                PlayerAsset buyerPA = playerAssetRepository.findByPlayerIdAndAsset_AssetId(buyerId, itemId);
                if (buyerPA == null) {
                    buyerPA = new PlayerAsset();
                    buyerPA.setPlayerId(buyerId);
                    buyerPA.setAsset(match.getAsset());
                    buyerPA.setQuantity(tradeQty);
                    buyerPA.setReservedQuantity(0);
                } else {
                    int q = buyerPA.getQuantity() == null ? 0 : buyerPA.getQuantity();
                    buyerPA.setQuantity(q + tradeQty);
                }
                playerAssetRepository.save(buyerPA);
            } else {
                // type == SELL (requester is seller)
                sellerId = requesterId;
                buyerId = match.getPlayerId();
                sellOrderId = order.getOrderId();
                buyOrderId = match.getOrderId();

                BigDecimal total = tradePrice.multiply(BigDecimal.valueOf(tradeQty));
                if (!walletService.hasSufficientFunds(buyerId, total)) {
                    continue;
                }
                walletService.debit(buyerId, total);
                walletService.credit(sellerId, total);
                // if the requester is seller (initiator), the buyer is the matched order owner; no refund logic here

                // Reduce reserved quantity on seller (requester)
                PlayerAsset sellerPA = playerAssetRepository.findByPlayerIdAndAsset_AssetId(sellerId, itemId);
                if (sellerPA != null) {
                    int reserved = sellerPA.getReservedQuantity() == null ? 0 : sellerPA.getReservedQuantity();
                    int q = sellerPA.getQuantity() == null ? 0 : sellerPA.getQuantity();
                    sellerPA.setReservedQuantity(Math.max(0, reserved - tradeQty));
                    sellerPA.setQuantity(Math.max(0, q - tradeQty));
                    playerAssetRepository.save(sellerPA);
                }

                // Give assets to buyer
                PlayerAsset buyerPA = playerAssetRepository.findByPlayerIdAndAsset_AssetId(buyerId, itemId);
                if (buyerPA == null) {
                    buyerPA = new PlayerAsset();
                    buyerPA.setPlayerId(buyerId);
                    buyerPA.setAsset(match.getAsset());
                    buyerPA.setQuantity(tradeQty);
                    buyerPA.setReservedQuantity(0);
                } else {
                    int q = buyerPA.getQuantity() == null ? 0 : buyerPA.getQuantity();
                    buyerPA.setQuantity(q + tradeQty);
                }
                playerAssetRepository.save(buyerPA);
            }

            // Create trade history
            TradeHistory th = new TradeHistory();
            th.setAsset(match.getAsset());
            th.setPrice(tradePrice);
            th.setQuantity(tradeQty);
            th.setBuyOrderId(buyOrderId);
            th.setSellOrderId(sellOrderId);
            tradeHistoryRepository.save(th);
            executedTrades.add(th);

            // Update matched order quantity/status
            int newMatchQty = matchQty - tradeQty;
            if (newMatchQty <= 0) {
                match.setQuantity(0);
                match.setStatus("FILLED");
            } else {
                match.setQuantity(newMatchQty);
                match.setStatus("PARTIALLY_FILLED");
            }
            orderRepository.save(match);

            remaining -= tradeQty;
        }

        // Update the initiating order status/quantity
        if (remaining <= 0) {
            order.setQuantity(0);
            order.setStatus("FILLED");
        } else if (remaining < amount) {
            order.setQuantity(remaining);
            order.setStatus("PARTIALLY_FILLED");
        } else {
            order.setQuantity(remaining);
            order.setStatus("OPEN");
        }
        orderRepository.save(order);

        // Under direct-debit semantics there is no reservation/refund step.

        return executedTrades;
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
            // No reserved funds under new semantics; nothing to release for BUY
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
