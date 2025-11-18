package com.example.market.service;

import com.example.market.domain.Currency;
import com.example.market.domain.LedgerEntry;
import com.example.market.domain.Player;
import com.example.market.domain.Wallet;
import com.example.market.repository.CurrencyRepository;
import com.example.market.repository.LedgerEntryRepository;
import com.example.market.repository.PlayerRepository;
import com.example.market.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    private final PlayerRepository playerRepo;
    private final CurrencyRepository currencyRepo;
    private final WalletRepository walletRepo;
    private final LedgerEntryRepository ledgerRepo;

    public WalletService(PlayerRepository playerRepo, CurrencyRepository currencyRepo, WalletRepository walletRepo, LedgerEntryRepository ledgerRepo) {
        this.playerRepo = playerRepo;
        this.currencyRepo = currencyRepo;
        this.walletRepo = walletRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Transactional
    public Wallet deposit(String username, String currencyCode, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        Player player = playerRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("player not found"));
        Currency currency = currencyRepo.findById(currencyCode).orElseThrow(() -> new IllegalArgumentException("currency not supported"));

        Wallet wallet = walletRepo.findByPlayerAndCurrencyForUpdate(player, currencyCode).orElseGet(() -> {
            Wallet w = new Wallet();
            w.setPlayer(player);
            w.setCurrency(currency);
            w.setBalance(0);
            return walletRepo.save(w);
        });

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepo.save(wallet);

        LedgerEntry le = new LedgerEntry();
        le.setWallet(wallet);
        le.setAmount(amount);
        le.setReason("deposit");
        ledgerRepo.save(le);

        return wallet;
    }

    @Transactional(readOnly = true)
    public java.util.List<Wallet> listWallets(String username) {
        if (!playerRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("player not found");
        }
        return walletRepo.findByPlayerUsername(username);
    }
}
