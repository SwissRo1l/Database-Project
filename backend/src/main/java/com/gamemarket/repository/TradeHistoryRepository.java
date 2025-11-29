package com.gamemarket.repository;

import com.gamemarket.entity.TradeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Integer> {
    @Query("SELECT t FROM TradeHistory t WHERE t.buyOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId) OR t.sellOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId)")
    List<TradeHistory> findByPlayerId(Integer playerId);

    @Query("SELECT t FROM TradeHistory t WHERE t.buyOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId) OR t.sellOrderId IN (SELECT o.orderId FROM MarketOrder o WHERE o.playerId = :playerId)")
    Page<TradeHistory> findByPlayerId(Integer playerId, Pageable pageable);

    List<TradeHistory> findByAsset_AssetIdOrderByTradeTimeAsc(Integer assetId);

    @Query(value = "SELECT CAST(trade_time AS DATE) as trade_date, MIN(price) as min_price FROM trade_history WHERE asset_id = :assetId GROUP BY CAST(trade_time AS DATE) ORDER BY trade_date ASC", nativeQuery = true)
    List<Object[]> findDailyMinPriceByAssetId(Integer assetId);
}
