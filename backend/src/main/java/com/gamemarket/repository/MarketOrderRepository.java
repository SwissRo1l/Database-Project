package com.gamemarket.repository;

import com.gamemarket.entity.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Integer> {
    List<MarketOrder> findByStatus(String status);
    List<MarketOrder> findByPlayerId(Integer playerId);
    List<MarketOrder> findByPlayerIdAndStatus(Integer playerId, String status);
    List<MarketOrder> findByAsset_AssetIdAndStatus(Integer assetId, String status);
}
