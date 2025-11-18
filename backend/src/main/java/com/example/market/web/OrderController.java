package com.example.market.web;

import com.example.market.domain.OrderEntity;
import com.example.market.service.OrderService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private record PurchaseRequest(@NotBlank String username, long listingId) {}
    private record OrdersQuery(@NotBlank String username) {}

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest body) {
        try {
            OrderEntity order = service.purchase(body.username(), body.listingId());
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "listing_id", order.getListing().getId(),
                    "item_id", order.getListing().getItem().getId(),
                    "price", order.getPricePaid()
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myOrders(@RequestParam("username") String username) {
        var list = service.listByBuyer(username);
        return ResponseEntity.ok(list.stream().map(o -> Map.of(
                "order_id", o.getId(),
                "listing_id", o.getListing().getId(),
                "item_id", o.getListing().getItem().getId(),
                "item_type", o.getListing().getItem().getType().getName(),
                "price", o.getPricePaid(),
                "currency_code", o.getCurrency().getCode(),
                "seller", o.getListing().getSeller().getUsername(),
                "created_at", o.getCreatedAt()
        )).toList());
    }
}
