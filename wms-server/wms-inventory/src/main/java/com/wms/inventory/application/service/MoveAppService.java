package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.common.util.DocNoUtil;
import com.wms.inventory.application.assembler.MoveAssembler;
import com.wms.inventory.application.dto.MoveCreateCmd;
import com.wms.inventory.application.dto.MoveDTO;
import com.wms.inventory.application.dto.MovePageQuery;
import com.wms.inventory.domain.entity.MoveHeader;
import com.wms.inventory.domain.entity.MoveLine;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.gateway.MasterDataGateway;
import com.wms.inventory.domain.repository.MoveRepository;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.service.StockDomainService;
import com.wms.inventory.infrastructure.mapper.MoveHeaderMapper;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveAppService {
    private final MoveRepository moveRepository;
    private final MoveHeaderMapper moveMapper;
    private final StockRepository stockRepository;
    private final StockDomainService stockDomainService;
    private final MoveAssembler assembler;
    private final MasterDataGateway masterDataGateway;

    public IPage<MoveDTO> pageMove(MovePageQuery query) {
        IPage<MoveHeader> result =
                moveMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<MoveHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        MoveHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        MoveHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(MoveHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public MoveDTO createMove(MoveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String moveNo = DocNoUtil.next("MV");

        // 创建移库单头
        MoveHeader header = new MoveHeader();
        header.setTenantId(tenantId);
        header.setWarehouseId(cmd.getWarehouseId());
        header.setMoveNo(moveNo);
        header.setMoveType(cmd.getMoveType());
        header.setStatus(MoveHeader.Status.CREATED);
        header.setRemark(cmd.getRemark());
        header.setCreatedBy(userId);
        header.setUpdatedBy(userId);
        moveRepository.saveHeader(header);

        // 扫码场景：前端可能传编码而非 ID，统一在此解析
        Sku sku = masterDataGateway.resolveSku(cmd.getSkuId(), cmd.getSkuCode(), tenantId);
        Long fromLocationId =
                masterDataGateway
                        .resolveLocation(
                                cmd.getFromLocationId(),
                                cmd.getFromLocationCode(),
                                cmd.getWarehouseId(),
                                tenantId)
                        .getId();
        Long toLocationId =
                masterDataGateway
                        .resolveLocation(
                                cmd.getToLocationId(),
                                cmd.getToLocationCode(),
                                cmd.getWarehouseId(),
                                tenantId)
                        .getId();

        // 创建移库单行 + 执行移动
        Stock fromStock =
                stockRepository
                        .findByKey(
                                tenantId,
                                cmd.getWarehouseId(),
                                fromLocationId,
                                sku.getId(),
                                cmd.getBatchNo())
                        .orElseThrow(() -> BusinessException.notFound("来源库位库存不存在或不足"));
        Stock toStock =
                stockRepository
                        .findByKey(
                                tenantId,
                                cmd.getWarehouseId(),
                                toLocationId,
                                sku.getId(),
                                cmd.getBatchNo())
                        .orElse(null);

        stockDomainService.moveStock(
                fromStock, toStock, toLocationId, cmd.getMoveQty(), header.getId(), moveNo, userId);

        MoveLine line = new MoveLine();
        line.setTenantId(tenantId);
        line.setMoveHeaderId(header.getId());
        line.setLineNo(1);
        line.setSkuId(sku.getId());
        line.setSkuCode(fromStock.getSkuCode());
        line.setSkuName(fromStock.getSkuName());
        line.setMoveQty(cmd.getMoveQty());
        line.setFromLocationId(fromLocationId);
        line.setFromStockId(fromStock.getId());
        line.setToLocationId(toLocationId);
        line.setBatchNo(cmd.getBatchNo());
        line.setStatus("DONE");
        line.setMoveBy(userId);
        line.setMoveAt(LocalDateTime.now());
        line.setCreatedBy(userId);
        line.setUpdatedBy(userId);
        moveRepository.saveLine(line);

        header.setStatus(MoveHeader.Status.DONE);
        moveRepository.updateHeader(header);

        return assembler.toDTO(header, List.of(line));
    }
}
