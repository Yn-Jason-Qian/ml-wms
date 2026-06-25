package com.wms.inbound.domain.repository;

import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.AsnLine;

import java.util.List;
import java.util.Optional;

public interface AsnRepository {
    Optional<AsnHeader> findById(Long id);

    List<AsnLine> findLinesByHeader(Long headerId);

    Optional<AsnLine> findLineById(Long id);

    void saveHeader(AsnHeader h);

    void updateHeader(AsnHeader h);

    void saveLine(AsnLine l);

    void updateLine(AsnLine l);

    boolean existsByAsnNo(Long tenantId, String asnNo, Long excludeId);
}
