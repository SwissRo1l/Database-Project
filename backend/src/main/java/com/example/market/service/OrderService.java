package com.example.market.service;

import com.example.market.domain.*;
import com.example.market.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final PlayerRepository playerRepo;
    private final ListingRepository listingRepo;
    private final WalletRepository walletRepo;
    private final OrderRepository orderRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final SettingRepository settingRepo;

    public OrderService(PlayerRepository playerRepo, ListingRepository listingRepo, WalletRepository walletRepo, OrderRepository orderRepo, LedgerEntryRepository ledgerRepo, SettingRepository settingRepo) {
        this.playerRepo = playerRepo;
        this.listingRepo = listingRepo;
        this.walletRepo = walletRepo;
        this.orderRepo = orderRepo;
        this.ledgerRepo = ledgerRepo;
        this.settingRepo = settingRepo;
    }

    @Transactional
    public OrderEntity purchase(String buyerUsername, long listingId) {
        var buyer = playerRepo.findByUsername(buyerUsername).orElseThrow(() -> new IllegalArgumentException("buyer not found"));
        var listing = listingRepo.findForUpdate(listingId).orElseThrow(() -> new IllegalArgumentException("listing not found"));
        if (!"active".equals(listing.getStatus())) {
            throw new IllegalStateException("listing not available");
        }
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("cannot buy your own listing");
        }

        var currencyCode = listing.getCurrency().getCode();
        long price = listing.getPrice();

        int feeBps = settingRepo.findById("market_fee_bps").map(s -> Integer.parseInt(s.getValue())).orElse(0);
        long fee = (price * feeBps) / 10000L;
        long sellerReceipt = price - fee;

        var buyerWallet = walletRepo.findByPlayerAndCurrencyForUpdate(buyer, currencyCode)
                .orElseThrow(() -> new IllegalStateException("buyer wallet not found"));
        if (buyerWallet.getBalance() < price) {
            throw new IllegalStateException("insufficient balance");
        }

        var sellerWallet = walletRepo.findByPlayerAndCurrencyForUpdate(listing.getSeller(), currencyCode)
                .orElseGet(() -> {
                    var w = new Wallet();
                    w.setPlayer(listing.getSeller());
                    w.setCurrency(listing.getCurrency());
                    w.setBalance(0);
                    return walletRepo.save(w);
                });

        // transfer funds
        buyerWallet.setBalance(buyerWallet.getBalance() - price);
        walletRepo.save(buyerWallet);

        var le1 = new LedgerEntry();
        le1.setWallet(buyerWallet);
        le1.setAmount(-price);
        le1.setReason("purchase");
        le1.setRefTable("listings");
        le1.setRefId(listing.getId());
        ledgerRepo.save(le1);

        sellerWallet.setBalance(sellerWallet.getBalance() + sellerReceipt);
        walletRepo.save(sellerWallet);

        var le2 = new LedgerEntry();
        le2.setWallet(sellerWallet);
        le2.setAmount(sellerReceipt);
        le2.setReason("sale_proceeds");
        le2.setRefTable("listings");
        le2.setRefId(listing.getId());
        ledgerRepo.save(le2);

        // update listing and item
        listing.setStatus("sold");
        var item = listing.getItem();
        item.setOwner(buyer);
        item.setStatus("sold");

        // persist order
        var order = new OrderEntity();
        order.setListing(listing);
        order.setBuyer(buyer);
        order.setCurrency(listing.getCurrency());
        order.setPricePaid(price);
        order.setFeeAmount(fee);
        order.setSellerReceipt(sellerReceipt);

        // JPA will flush changes; repositories ensure persist
        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderEntity> listByBuyer(String username) {
        return orderRepo.findByBuyerUsernameOrderByCreatedAtDesc(username);
    }
}
