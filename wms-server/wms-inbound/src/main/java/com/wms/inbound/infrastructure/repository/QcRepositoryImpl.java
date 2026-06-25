package com.wms.inbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inbound.domain.entity.QcHeader;
import com.wms.inbound.domain.entity.QcLine;
import com.wms.inbound.domain.repository.QcRepository;
import com.wms.inbound.infrastructure.mapper.QcHeaderMapper;
import com.wms.inbound.infrastructure.mapper.QcLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QcRepositoryImpl implements QcRepository {
    private final QcHeaderMapper headerMapper;
    private final QcLineMapper lineMapper;

    @Override
    public Optional<QcHeader> findById(Long id) {
        return Optional.ofNullable(headerMapper.selectById(id));
    }

    @Override
    public void saveHeader(QcHeader h) {
        headerMapper.insert(h);
    }

    @Override
    public void saveLine(QcLine l) {
        lineMapper.insert(l);
    }

    @Override
    public List<QcLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(
                new LambdaQueryWrapper<QcLine>().eq(QcLine::getQcHeaderId, headerId));
    }

    @Override
    public void updateHeader(QcHeader h) {
        headerMapper.updateById(h);
    }
}
