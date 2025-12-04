package com.gamemarket.controller;

import java.math.BigDecimal;

public class RechargeRequest {
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
