package com.gamemarket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_asset")
public class PlayerAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "purchase_date")
    private java.time.LocalDateTime purchaseDate = java.time.LocalDateTime.now();

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public java.time.LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(java.time.LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}
