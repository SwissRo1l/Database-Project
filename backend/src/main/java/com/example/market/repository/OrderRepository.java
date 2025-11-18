package com.example.market.repository;

import com.example.market.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	List<OrderEntity> findByBuyerUsernameOrderByCreatedAtDesc(String username);
}
