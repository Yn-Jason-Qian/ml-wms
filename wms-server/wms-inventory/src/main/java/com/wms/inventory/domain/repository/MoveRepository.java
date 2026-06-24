package com.wms.inventory.domain.repository;

import com.wms.inventory.domain.entity.MoveHeader;
import com.wms.inventory.domain.entity.MoveLine;

import java.util.List;
import java.util.Optional;

public interface MoveRepository {
    Optional<MoveHeader> findHeaderById(Long id);

    void saveHeader(MoveHeader header);

    void updateHeader(MoveHeader header);

    void saveLine(MoveLine line);

    void updateLine(MoveLine line);

    List<MoveLine> findLinesByHeader(Long headerId);

    Optional<MoveLine> findLineById(Long id);

    boolean existsByNo(Long tenantId, String moveNo, Long excludeId);
}
