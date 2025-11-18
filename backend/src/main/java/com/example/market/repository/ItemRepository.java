package com.example.market.repository;

import com.example.market.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Item> findWithLockingById(Long id);

    java.util.List<Item> findByOwnerUsernameOrderByCreatedAtDesc(String username);
}
