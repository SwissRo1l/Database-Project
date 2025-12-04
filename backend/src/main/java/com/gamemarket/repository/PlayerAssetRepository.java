package com.gamemarket.repository;

import com.gamemarket.entity.PlayerAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerAssetRepository extends JpaRepository<PlayerAsset, Integer> {
    List<PlayerAsset> findByPlayerId(Integer playerId);

    PlayerAsset findByPlayerIdAndAsset_AssetId(Integer playerId, Integer assetId);
}
