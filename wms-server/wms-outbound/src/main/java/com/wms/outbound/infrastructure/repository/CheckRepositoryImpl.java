package com.wms.outbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.outbound.domain.entity.CheckHeader;
import com.wms.outbound.domain.entity.CheckLine;
import com.wms.outbound.domain.repository.CheckRepository;
import com.wms.outbound.infrastructure.mapper.CheckHeaderMapper;
import com.wms.outbound.infrastructure.mapper.CheckLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckRepositoryImpl implements CheckRepository {
    private final CheckHeaderMapper checkMapper;
    private final CheckLineMapper checkLineMapper;

    @Override
    public Optional<CheckHeader> findById(Long id) {
        return Optional.ofNullable(checkMapper.selectById(id));
    }

    @Override
    public Optional<CheckHeader> findByWave(Long waveId) {
        return Optional.ofNullable(
                checkMapper.selectOne(
                        new LambdaQueryWrapper<CheckHeader>()
                                .eq(CheckHeader::getWaveHeaderId, waveId)
                                .orderByDesc(CheckHeader::getCreatedAt)
                                .last("LIMIT 1")));
    }

    @Override
    public void saveHeader(CheckHeader h) {
        checkMapper.insert(h);
    }

    @Override
    public void saveLine(CheckLine l) {
        checkLineMapper.insert(l);
    }

    @Override
    public void updateLine(CheckLine l) {
        checkLineMapper.updateById(l);
    }

    @Override
    public void updateHeader(CheckHeader h) {
        checkMapper.updateById(h);
    }

    @Override
    public List<CheckLine> findLines(Long headerId) {
        return checkLineMapper.selectList(
                new LambdaQueryWrapper<CheckLine>()
                        .eq(CheckLine::getCheckHeaderId, headerId)
                        .orderByAsc(CheckLine::getLineNo));
    }
}
