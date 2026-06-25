package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.WaveHeader;
import com.wms.outbound.domain.entity.WaveLine;

import java.util.List;
import java.util.Optional;

public interface WaveRepository {
    Optional<WaveHeader> findById(Long id);

    void save(WaveHeader h);

    void update(WaveHeader h);

    void saveLine(WaveLine l);

    List<WaveLine> findLines(Long waveId);
}
