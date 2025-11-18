package com.example.market.service;

import com.example.market.domain.*;
import com.example.market.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListingService {
    private final PlayerRepository playerRepo;
    private final ItemRepository itemRepo;
    private final ListingRepository listingRepo;
    private final CurrencyRepository currencyRepo;

    public ListingService(PlayerRepository playerRepo, ItemRepository itemRepo, ListingRepository listingRepo, CurrencyRepository currencyRepo) {
        this.playerRepo = playerRepo;
        this.itemRepo = itemRepo;
        this.listingRepo = listingRepo;
        this.currencyRepo = currencyRepo;
    }

    public List<Listing> listActive() {
        return listingRepo.findByStatusOrderByCreatedAtDesc("active");
    }

    @Transactional
    public Listing createListing(String username, long itemId, String currencyCode, long price) {
        var player = playerRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("player not found"));
        var item = itemRepo.findWithLockingById(itemId).orElseThrow(() -> new IllegalArgumentException("item not found"));
        if (item.getOwner() == null || !item.getOwner().getId().equals(player.getId())) {
            throw new IllegalArgumentException("item not owned by player");
        }
        if (!"in_inventory".equals(item.getStatus())) {
            throw new IllegalStateException("item not eligible for listing");
        }
        var currency = currencyRepo.findById(currencyCode).orElseThrow(() -> new IllegalArgumentException("currency not supported"));

        item.setStatus("listed");
        itemRepo.save(item);

        var listing = new Listing();
        listing.setItem(item);
        listing.setSeller(player);
        listing.setCurrency(currency);
        listing.setPrice(price);
        listing.setStatus("active");
        return listingRepo.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> listBySeller(String username) {
        return listingRepo.findBySellerUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional
    public Listing cancel(String username, long listingId) {
        var player = playerRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("player not found"));
        var listing = listingRepo.findForUpdate(listingId).orElseThrow(() -> new IllegalArgumentException("listing not found"));
        if (!"active".equals(listing.getStatus())) {
            throw new IllegalStateException("listing not active");
        }
        if (!listing.getSeller().getId().equals(player.getId())) {
            throw new IllegalArgumentException("not seller of listing");
        }
        var item = listing.getItem();
        item.setStatus("in_inventory");
        itemRepo.save(item);

        listing.setStatus("cancelled");
        return listingRepo.save(listing);
    }
}
