package com.example.market.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "item_id", unique = true)
    private Item item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id")
    private Player seller;

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_code")
    private Currency currency;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false, length = 16)
    private String status = "active"; // active|sold|cancelled|expired

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
