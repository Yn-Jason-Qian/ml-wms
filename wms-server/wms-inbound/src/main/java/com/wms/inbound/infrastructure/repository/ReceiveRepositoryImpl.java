package com.wms.inbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;
import com.wms.inbound.domain.repository.ReceiveRepository;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReceiveRepositoryImpl implements ReceiveRepository {
    private final ReceiveHeaderMapper headerMapper;
    private final ReceiveLineMapper lineMapper;

    @Override
    public Optional<ReceiveHeader> findById(Long id) {
        return Optional.ofNullable(headerMapper.selectById(id));
    }

    @Override
    public void saveHeader(ReceiveHeader h) {
        headerMapper.insert(h);
    }

    @Override
    public void updateHeader(ReceiveHeader h) {
        headerMapper.updateById(h);
    }

    @Override
    public void saveLine(ReceiveLine l) {
        lineMapper.insert(l);
    }

    @Override
    public List<ReceiveLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(
                new LambdaQueryWrapper<ReceiveLine>()
                        .eq(ReceiveLine::getReceiveHeaderId, headerId));
    }
}
