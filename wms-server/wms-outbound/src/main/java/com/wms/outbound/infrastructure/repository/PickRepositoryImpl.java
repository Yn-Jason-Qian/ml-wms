package com.wms.outbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.outbound.domain.entity.PickHeader;
import com.wms.outbound.domain.entity.PickLine;
import com.wms.outbound.domain.repository.PickRepository;
import com.wms.outbound.infrastructure.mapper.PickHeaderMapper;
import com.wms.outbound.infrastructure.mapper.PickLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PickRepositoryImpl implements PickRepository {
    private final PickHeaderMapper pickMapper;
    private final PickLineMapper pickLineMapper;

    @Override
    public Optional<PickHeader> findById(Long id) {
        return Optional.ofNullable(pickMapper.selectById(id));
    }

    @Override
    public Optional<PickHeader> findByWave(Long waveId) {
        return Optional.ofNullable(
                pickMapper.selectOne(
                        new LambdaQueryWrapper<PickHeader>()
                                .eq(PickHeader::getWaveHeaderId, waveId)
                                .orderByDesc(PickHeader::getCreatedAt)
                                .last("LIMIT 1")));
    }

    @Override
    public void saveHeader(PickHeader h) {
        pickMapper.insert(h);
    }

    @Override
    public void saveLine(PickLine l) {
        pickLineMapper.insert(l);
    }

    @Override
    public void updateLine(PickLine l) {
        pickLineMapper.updateById(l);
    }

    @Override
    public void updateHeader(PickHeader h) {
        pickMapper.updateById(h);
    }

    @Override
    public List<PickLine> findLines(Long hId) {
        return pickLineMapper.selectList(
                new LambdaQueryWrapper<PickLine>().eq(PickLine::getPickHeaderId, hId));
    }
}
