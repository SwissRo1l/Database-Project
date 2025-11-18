package com.example.market.web;

import com.example.market.domain.Item;
import com.example.market.service.ItemService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private record MintRequest(@NotBlank String username, @NotBlank String itemType, String metadata) {}

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping("/mint")
    public ResponseEntity<?> mint(@RequestBody MintRequest body) {
        try {
            Item it = service.mint(body.username(), body.itemType(), body.metadata());
            return ResponseEntity.ok(Map.of("item_id", it.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listByOwner(@RequestParam(name = "owner", required = false) String owner) {
        if (owner == null || owner.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "owner is required"));
        }
        var items = service.findByOwner(owner);
        return ResponseEntity.ok(items.stream().map(i -> Map.of(
                "id", i.getId(),
                "type", i.getType().getName(),
                "status", i.getStatus(),
                "metadata", i.getMetadata()
        )).toList());
    }
}
