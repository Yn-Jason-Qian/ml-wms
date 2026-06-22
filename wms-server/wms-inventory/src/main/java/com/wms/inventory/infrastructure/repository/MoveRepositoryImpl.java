package com.wms.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inventory.domain.entity.MoveHeader;
import com.wms.inventory.domain.entity.MoveLine;
import com.wms.inventory.domain.repository.MoveRepository;
import com.wms.inventory.infrastructure.mapper.MoveHeaderMapper;
import com.wms.inventory.infrastructure.mapper.MoveLineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MoveRepositoryImpl implements MoveRepository {
    private final MoveHeaderMapper headerMapper;
    private final MoveLineMapper lineMapper;

    @Override public Optional<MoveHeader> findHeaderById(Long id) { return Optional.ofNullable(headerMapper.selectById(id)); }
    @Override public void saveHeader(MoveHeader h) { headerMapper.insert(h); }
    @Override public void updateHeader(MoveHeader h) { headerMapper.updateById(h); }
    @Override public void saveLine(MoveLine l) { lineMapper.insert(l); }
    @Override public void updateLine(MoveLine l) { lineMapper.updateById(l); }

    @Override
    public List<MoveLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(new LambdaQueryWrapper<MoveLine>()
                .eq(MoveLine::getMoveHeaderId, headerId)
                .orderByAsc(MoveLine::getLineNo));
    }

    @Override public Optional<MoveLine> findLineById(Long id) { return Optional.ofNullable(lineMapper.selectById(id)); }

    @Override
    public boolean existsByNo(Long tenantId, String moveNo, Long excludeId) {
Long count = headerMapper.selectCount(new LambdaQueryWrapper<MoveHeader>()
                .eq(MoveHeader::getTenantId, tenantId)
                .eq(MoveHeader::getMoveNo, moveNo)
                .ne(excludeId != null, MoveHeader::getId, excludeId));
        return count > 0;
    }
}
