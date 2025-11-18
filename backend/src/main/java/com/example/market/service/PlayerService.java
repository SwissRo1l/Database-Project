package com.example.market.service;

import com.example.market.domain.Player;
import com.example.market.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {
    private final PlayerRepository repo;

    public PlayerService(PlayerRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Player register(String username) {
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("username already exists");
        }
        Player p = new Player();
        p.setUsername(username);
        return repo.save(p);
    }
}
