package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.inventory.application.assembler.StocktakeAssembler;
import com.wms.inventory.application.dto.StocktakeCreateCmd;
import com.wms.inventory.application.dto.StocktakeDTO;
import com.wms.inventory.application.dto.StocktakePageQuery;
import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.domain.repository.StocktakeRepository;
import com.wms.inventory.infrastructure.mapper.StocktakeHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeAppService {
    private final StocktakeRepository stocktakeRepository;
    private final StocktakeHeaderMapper stocktakeMapper;
    private final StocktakeAssembler assembler;

    public IPage<StocktakeDTO> pageStocktake(StocktakePageQuery query) {
        IPage<StocktakeHeader> result =
                stocktakeMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<StocktakeHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        StocktakeHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        StocktakeHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(StocktakeHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public StocktakeDTO createStocktake(StocktakeCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String no =
                "ST-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        StocktakeHeader h = new StocktakeHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setStocktakeNo(no);
        h.setStocktakeType(cmd.getStocktakeType());
        h.setStocktakeMode(cmd.getStocktakeMode());
        h.setLocationRange(cmd.getLocationRange());
        h.setPlanStartTime(cmd.getPlanStartTime());
        h.setPlanEndTime(cmd.getPlanEndTime());
        h.setStatus(StocktakeHeader.Status.CREATED);
        h.setTotalLines(0);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        stocktakeRepository.saveHeader(h);
        return assembler.toDTO(h, List.of());
    }
}
