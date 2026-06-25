package com.wms.outbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.outbound.domain.entity.WaveHeader;
import com.wms.outbound.domain.entity.WaveLine;
import com.wms.outbound.domain.repository.WaveRepository;
import com.wms.outbound.infrastructure.mapper.WaveHeaderMapper;
import com.wms.outbound.infrastructure.mapper.WaveLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WaveRepositoryImpl implements WaveRepository {
    private final WaveHeaderMapper waveMapper;
    private final WaveLineMapper waveLineMapper;

    @Override
    public Optional<WaveHeader> findById(Long id) {
        return Optional.ofNullable(waveMapper.selectById(id));
    }

    @Override
    public void save(WaveHeader h) {
        waveMapper.insert(h);
    }

    @Override
    public void update(WaveHeader h) {
        waveMapper.updateById(h);
    }

    @Override
    public void saveLine(WaveLine l) {
        waveLineMapper.insert(l);
    }

    @Override
    public List<WaveLine> findLines(Long wId) {
        return waveLineMapper.selectList(
                new LambdaQueryWrapper<WaveLine>().eq(WaveLine::getWaveHeaderId, wId));
    }
}
