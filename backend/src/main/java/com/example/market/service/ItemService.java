package com.example.market.service;

import com.example.market.domain.Item;
import com.example.market.domain.ItemType;
import com.example.market.domain.Player;
import com.example.market.repository.ItemTypeRepository;
import com.example.market.repository.PlayerRepository;
import com.example.market.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {
    private final PlayerRepository playerRepo;
    private final ItemTypeRepository typeRepo;
    private final ItemRepository itemRepo;

    public ItemService(PlayerRepository playerRepo, ItemTypeRepository typeRepo, ItemRepository itemRepo) {
        this.playerRepo = playerRepo;
        this.typeRepo = typeRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional
    public Item mint(String username, String itemTypeName, String metadata) {
        Player p = playerRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("player not found"));
        ItemType t = typeRepo.findByName(itemTypeName).orElseThrow(() -> new IllegalArgumentException("item type not found"));
        Item i = new Item();
        i.setType(t);
        i.setOwner(p);
        i.setStatus("in_inventory");
        i.setMetadata(metadata);
        return itemRepo.save(i);
    }

    @Transactional(readOnly = true)
    public java.util.List<Item> findByOwner(String username) {
        if (!playerRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("player not found");
        }
        return itemRepo.findByOwnerUsernameOrderByCreatedAtDesc(username);
    }
}
