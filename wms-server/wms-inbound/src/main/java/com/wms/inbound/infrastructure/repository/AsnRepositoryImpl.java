package com.wms.inbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.AsnLine;
import com.wms.inbound.domain.repository.AsnRepository;
import com.wms.inbound.infrastructure.mapper.AsnHeaderMapper;
import com.wms.inbound.infrastructure.mapper.AsnLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AsnRepositoryImpl implements AsnRepository {
    private final AsnHeaderMapper headerMapper;
    private final AsnLineMapper lineMapper;

    @Override
    public Optional<AsnHeader> findById(Long id) {
        return Optional.ofNullable(headerMapper.selectById(id));
    }

    @Override
    public List<AsnLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(
                new LambdaQueryWrapper<AsnLine>()
                        .eq(AsnLine::getAsnHeaderId, headerId)
                        .orderByAsc(AsnLine::getLineNo));
    }

    @Override
    public Optional<AsnLine> findLineById(Long id) {
        return Optional.ofNullable(lineMapper.selectById(id));
    }

    @Override
    public void saveHeader(AsnHeader h) {
        headerMapper.insert(h);
    }

    @Override
    public void updateHeader(AsnHeader h) {
        headerMapper.updateById(h);
    }

    @Override
    public void saveLine(AsnLine l) {
        lineMapper.insert(l);
    }

    @Override
    public void updateLine(AsnLine l) {
        lineMapper.updateById(l);
    }

    @Override
    public boolean existsByAsnNo(Long tenantId, String no, Long excludeId) {
        return headerMapper.selectCount(
                        new LambdaQueryWrapper<AsnHeader>()
                                .eq(AsnHeader::getTenantId, tenantId)
                                .eq(AsnHeader::getAsnNo, no)
                                .ne(excludeId != null, AsnHeader::getId, excludeId))
                > 0;
    }
}
