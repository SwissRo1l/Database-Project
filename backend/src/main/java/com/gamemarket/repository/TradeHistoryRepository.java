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

    // Sum of quantities for an asset since a given timestamp (used for 24h sales)
    @Query(value = "SELECT COALESCE(SUM(quantity),0) FROM trade_history WHERE asset_id = :assetId AND trade_time >= :since", nativeQuery = true)
    Long sumQuantitySince(Integer assetId, java.sql.Timestamp since);

    // Latest price for an asset (most recent trade)
    @Query(value = "SELECT price FROM trade_history WHERE asset_id = :assetId ORDER BY trade_time DESC LIMIT 1", nativeQuery = true)
    Double findLatestPriceByAssetId(Integer assetId);

    // Price at or before a given timestamp (useful for price 24h ago)
    @Query(value = "SELECT price FROM trade_history WHERE asset_id = :assetId AND trade_time <= :before ORDER BY trade_time DESC LIMIT 1", nativeQuery = true)
    Double findPriceAtOrBefore(Integer assetId, java.sql.Timestamp before);

    @Query(value = "SELECT date_trunc('hour', trade_time) AS hour_slot, AVG(price) AS avg_price FROM trade_history WHERE asset_id = :assetId AND trade_time >= :since GROUP BY hour_slot ORDER BY hour_slot ASC", nativeQuery = true)
    List<Object[]> findHourlyAvgPriceByAssetIdSince(Integer assetId, java.sql.Timestamp since);
}
