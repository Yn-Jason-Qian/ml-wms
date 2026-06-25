package com.wms.inbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inbound.domain.entity.PutawayHeader;
import com.wms.inbound.domain.entity.PutawayLine;
import com.wms.inbound.domain.repository.PutawayRepository;
import com.wms.inbound.infrastructure.mapper.PutawayHeaderMapper;
import com.wms.inbound.infrastructure.mapper.PutawayLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PutawayRepositoryImpl implements PutawayRepository {
    private final PutawayHeaderMapper headerMapper;
    private final PutawayLineMapper lineMapper;

    @Override
    public Optional<PutawayHeader> findById(Long id) {
        return Optional.ofNullable(headerMapper.selectById(id));
    }

    @Override
    public void saveHeader(PutawayHeader h) {
        headerMapper.insert(h);
    }

    @Override
    public void saveLine(PutawayLine l) {
        lineMapper.insert(l);
    }

    @Override
    public void updateLine(PutawayLine l) {
        lineMapper.updateById(l);
    }

    @Override
    public List<PutawayLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(
                new LambdaQueryWrapper<PutawayLine>()
                        .eq(PutawayLine::getPutawayHeaderId, headerId));
    }

    @Override
    public void updateHeader(PutawayHeader h) {
        headerMapper.updateById(h);
    }
}
