package com.gamemarket.repository;

import com.gamemarket.entity.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Integer> {
    @Query("SELECT t FROM TradeHistory t WHERE t.buyOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId) OR t.sellOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId)")
    List<TradeHistory> findByPlayerId(Integer playerId);

    List<TradeHistory> findByAsset_AssetIdOrderByTradeTimeAsc(Integer assetId);
}
