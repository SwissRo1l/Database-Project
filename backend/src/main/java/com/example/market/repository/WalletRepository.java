package com.example.market.repository;

import com.example.market.domain.Player;
import com.example.market.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Query("select w from Wallet w where w.player = :player and w.currency.code = :code")
    Optional<Wallet> findByPlayerAndCurrency(@Param("player") Player player, @Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.player = :player and w.currency.code = :code")
    Optional<Wallet> findByPlayerAndCurrencyForUpdate(@Param("player") Player player, @Param("code") String code);

    @Query("select w from Wallet w where w.player.username = :username order by w.currency.code")
    java.util.List<Wallet> findByPlayerUsername(@Param("username") String username);
}
