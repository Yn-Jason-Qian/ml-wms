package com.wms.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.domain.entity.StocktakeLine;
import com.wms.inventory.domain.repository.StocktakeRepository;
import com.wms.inventory.infrastructure.mapper.StocktakeHeaderMapper;
import com.wms.inventory.infrastructure.mapper.StocktakeLineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StocktakeRepositoryImpl implements StocktakeRepository {
    private final StocktakeHeaderMapper headerMapper;
    private final StocktakeLineMapper lineMapper;

    @Override public Optional<StocktakeHeader> findHeaderById(Long id) { return Optional.ofNullable(headerMapper.selectById(id)); }
    @Override public void saveHeader(StocktakeHeader h) { headerMapper.insert(h); }
    @Override public void updateHeader(StocktakeHeader h) { headerMapper.updateById(h); }
    @Override public void saveLine(StocktakeLine l) { lineMapper.insert(l); }
    @Override public void updateLine(StocktakeLine l) { lineMapper.updateById(l); }

    @Override
    public List<StocktakeLine> findLinesByHeader(Long headerId) {
        return lineMapper.selectList(new LambdaQueryWrapper<StocktakeLine>()
                .eq(StocktakeLine::getStocktakeHeaderId, headerId)
                .orderByAsc(StocktakeLine::getLineNo));
    }

    @Override public Optional<StocktakeLine> findLineById(Long id) { return Optional.ofNullable(lineMapper.selectById(id)); }

    @Override
    public boolean existsByNo(Long tenantId, String stocktakeNo, Long excludeId) {
Long count = headerMapper.selectCount(new LambdaQueryWrapper<StocktakeHeader>()
                .eq(StocktakeHeader::getTenantId, tenantId)
                .eq(StocktakeHeader::getStocktakeNo, stocktakeNo)
                .ne(excludeId != null, StocktakeHeader::getId, excludeId));
        return count > 0;
    }
}
