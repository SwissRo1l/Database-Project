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
        BigDecimal balance = w.getBalance() == null ? BigDecimal.ZERO : w.getBalance();
        // available funds are represented directly by balance (we will move funds into reserved)
        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        // deduct from balance and add to reserved so balance reflects immediately reduced available funds
        w.setBalance(balance.subtract(amount));
        w.setReserved(reserved.add(amount));
        walletRepository.save(w);
        System.out.printf("[Wallet] reserveFunds: player=%d amount=%s newBalance=%s reserved=%s\n", playerId, amount, w.getBalance(), w.getReserved());
    }

    @Transactional
    public void releaseReserved(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found");
        }
        BigDecimal reserved = w.getReserved() == null ? BigDecimal.ZERO : w.getReserved();
        BigDecimal balance = w.getBalance() == null ? BigDecimal.ZERO : w.getBalance();
        BigDecimal newReserved = reserved.subtract(amount);
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) {
            newReserved = BigDecimal.ZERO;
        }
        // return funds to balance
        w.setReserved(newReserved);
        w.setBalance(balance.add(amount));
        walletRepository.save(w);
        System.out.printf("[Wallet] releaseReserved: player=%d amount=%s newBalance=%s reserved=%s\n", playerId, amount, w.getBalance(), w.getReserved());
    }

    @Transactional
    public void commitReserved(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found");
        }
        BigDecimal reserved = w.getReserved() == null ? BigDecimal.ZERO : w.getReserved();
        BigDecimal newReserved = reserved.subtract(amount);
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) {
            newReserved = BigDecimal.ZERO;
        }
        // Funds were already moved out of balance into reserved at reservation time.
        // On commit we only reduce the reserved amount (finalize the spend).
        w.setReserved(newReserved);
        walletRepository.save(w);
        System.out.printf("[Wallet] commitReserved: player=%d amount=%s newReserved=%s\n", playerId, amount, w.getReserved());
    }

    @Transactional
    public Wallet recharge(Integer playerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid recharge amount");
        }
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            w = new Wallet();
            w.setPlayerId(playerId);
            w.setBalance(amount);
            w.setReserved(BigDecimal.ZERO);
        } else {
            BigDecimal bal = w.getBalance() == null ? BigDecimal.ZERO : w.getBalance();
            w.setBalance(bal.add(amount));
        }
        return walletRepository.save(w);
    }

    @Transactional
    public void credit(Integer playerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid credit amount");
        }
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            w = new Wallet();
            w.setPlayerId(playerId);
            w.setBalance(amount);
            w.setReserved(BigDecimal.ZERO);
        } else {
            BigDecimal bal = w.getBalance() == null ? BigDecimal.ZERO : w.getBalance();
            w.setBalance(bal.add(amount));
        }
        walletRepository.save(w);
        System.out.printf("[Wallet] credit: player=%d amount=%s newBalance=%s\n", playerId, amount, w.getBalance());
    }

    @Transactional
    public void debit(Integer playerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid debit amount");
        }
        Wallet w = walletRepository.findByPlayerId(playerId);
        if (w == null) {
            throw new RuntimeException("Wallet not found for player " + playerId);
        }
        BigDecimal bal = w.getBalance() == null ? BigDecimal.ZERO : w.getBalance();
        if (bal.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        w.setBalance(bal.subtract(amount));
        walletRepository.save(w);
        System.out.printf("[Wallet] debit: player=%d amount=%s newBalance=%s\n", playerId, amount, w.getBalance());
    }

    public boolean hasSufficientFunds(Integer playerId, BigDecimal amount) {
        Wallet w = walletRepository.findByPlayerId(playerId);
        BigDecimal bal = (w == null || w.getBalance() == null) ? BigDecimal.ZERO : w.getBalance();
        return bal.compareTo(amount) >= 0;
    }
}
