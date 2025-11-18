package com.example.market.web;

import com.example.market.domain.Listing;
import com.example.market.service.ListingService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private record CreateListingRequest(@NotBlank String username, long itemId, @NotBlank String currency, @Min(1) long price) {}
    private record CancelRequest(@NotBlank String username) {}

    private final ListingService service;

    public ListingController(ListingService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> listActive() {
        return service.listActive().stream().map(this::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateListingRequest body) {
        try {
            if (body.price() < 1) {
                return ResponseEntity.badRequest().body(Map.of("error", "price must be >= 1"));
            }
            var l = service.createListing(body.username(), body.itemId(), body.currency(), body.price());
            return ResponseEntity.ok(Map.of("listing_id", l.getId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private Map<String, Object> toDto(Listing l) {
        return Map.of(
                "listing_id", l.getId(),
                "price", l.getPrice(),
                "currency_code", l.getCurrency().getCode(),
                "item_id", l.getItem().getId(),
                "item_type", l.getItem().getType().getName(),
                "seller", l.getSeller().getUsername()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myListings(@RequestParam("username") String username) {
        var list = service.listBySeller(username);
        return ResponseEntity.ok(list.stream().map(this::toDto).toList());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("id") long id, @RequestBody CancelRequest body) {
        try {
            var l = service.cancel(body.username(), id);
            return ResponseEntity.ok(Map.of("listing_id", l.getId(), "status", l.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
