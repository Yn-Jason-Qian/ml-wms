package com.wms.inbound.domain.repository;

import com.wms.inbound.domain.entity.PutawayHeader;
import com.wms.inbound.domain.entity.PutawayLine;

import java.util.List;
import java.util.Optional;

public interface PutawayRepository {
    Optional<PutawayHeader> findById(Long id);

    void saveHeader(PutawayHeader h);

    void saveLine(PutawayLine l);

    void updateLine(PutawayLine l);

    List<PutawayLine> findLinesByHeader(Long headerId);

    void updateHeader(PutawayHeader h);
}
