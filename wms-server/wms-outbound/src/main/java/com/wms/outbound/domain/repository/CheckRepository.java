package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.CheckHeader;
import com.wms.outbound.domain.entity.CheckLine;

import java.util.List;
import java.util.Optional;

public interface CheckRepository {
    Optional<CheckHeader> findById(Long id);

    Optional<CheckHeader> findByWave(Long waveId);

    void saveHeader(CheckHeader h);

    void saveLine(CheckLine l);

    void updateLine(CheckLine l);

    void updateHeader(CheckHeader h);

    List<CheckLine> findLines(Long headerId);
}
