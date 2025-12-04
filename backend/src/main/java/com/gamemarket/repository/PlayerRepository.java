package com.gamemarket.repository;

import com.gamemarket.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Player findByPlayerName(String playerName);
    Player findByEmail(String email);
}
