package com.gamemarket.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_history")
public class TradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Integer tradeId;

    @Column(name = "buy_order_id", nullable = false)
    private Integer buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Integer sellOrderId;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "trade_time")
    private LocalDateTime tradeTime = LocalDateTime.now();

    // Getters and Setters
    public Integer getTradeId() { return tradeId; }
    public void setTradeId(Integer tradeId) { this.tradeId = tradeId; }
    public Integer getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(Integer buyOrderId) { this.buyOrderId = buyOrderId; }
    public Integer getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(Integer sellOrderId) { this.sellOrderId = sellOrderId; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getTradeTime() { return tradeTime; }
    public void setTradeTime(LocalDateTime tradeTime) { this.tradeTime = tradeTime; }
}
