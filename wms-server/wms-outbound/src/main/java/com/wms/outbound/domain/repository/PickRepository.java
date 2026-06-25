package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.PickHeader;
import com.wms.outbound.domain.entity.PickLine;

import java.util.List;
import java.util.Optional;

public interface PickRepository {
    Optional<PickHeader> findById(Long id);

    Optional<PickHeader> findByWave(Long waveId);

    void saveHeader(PickHeader h);

    void saveLine(PickLine l);

    void updateLine(PickLine l);

    void updateHeader(PickHeader h);

    List<PickLine> findLines(Long headerId);
}
