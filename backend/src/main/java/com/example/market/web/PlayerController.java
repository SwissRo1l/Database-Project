package com.example.market.web;

import com.example.market.domain.Player;
import com.example.market.service.PlayerService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private record CreatePlayerRequest(@NotBlank String username) {}

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePlayerRequest body) {
        try {
            Player p = service.register(body.username());
            return ResponseEntity.ok(Map.of("id", p.getId(), "username", p.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
