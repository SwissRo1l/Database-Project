package com.gamemarket.service;

import com.gamemarket.entity.Wallet;
import com.gamemarket.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Transactional
    public void reserveFunds(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found for player " + playerId);
        }
        BigDecimal reserved = w.getReserved() == null ? BigDecimal.ZERO : w.getReserved();
        BigDecimal available = w.getBalance().subtract(reserved);
        if (available.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        w.setReserved(reserved.add(amount));
        walletRepository.save(w);
    }

    @Transactional
    public void releaseReserved(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found");
        }
        BigDecimal reserved = w.getReserved() == null ? BigDecimal.ZERO : w.getReserved();
        w.setReserved(reserved.subtract(amount));
        walletRepository.save(w);
    }

    @Transactional
    public void commitReserved(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found");
        }
        BigDecimal reserved = w.getReserved() == null ? BigDecimal.ZERO : w.getReserved();
        w.setReserved(reserved.subtract(amount));
        w.setBalance(w.getBalance().subtract(amount));
        walletRepository.save(w);
    }
}
