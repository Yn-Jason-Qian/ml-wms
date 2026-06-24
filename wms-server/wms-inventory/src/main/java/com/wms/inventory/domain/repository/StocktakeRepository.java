package com.wms.inventory.domain.repository;

import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.domain.entity.StocktakeLine;

import java.util.List;
import java.util.Optional;

public interface StocktakeRepository {
    Optional<StocktakeHeader> findHeaderById(Long id);

    void saveHeader(StocktakeHeader header);

    void updateHeader(StocktakeHeader header);

    void saveLine(StocktakeLine line);

    void updateLine(StocktakeLine line);

    List<StocktakeLine> findLinesByHeader(Long headerId);

    Optional<StocktakeLine> findLineById(Long id);

    boolean existsByNo(Long tenantId, String stocktakeNo, Long excludeId);
}
