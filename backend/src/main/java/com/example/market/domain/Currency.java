package com.example.market.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
public class Currency {
    @Id
    @Column(length = 16)
    private String code; // e.g., GOLD

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false)
    private short decimals;
}
