package com.wms.inbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inbound.application.assembler.ReceiveAssembler;
import com.wms.inbound.application.dto.ReceiveCreateCmd;
import com.wms.inbound.application.dto.ReceiveDTO;
import com.wms.inbound.application.dto.ReceivePageQuery;
import com.wms.inbound.application.dto.ReceiveResultDTO;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;
import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.inbound.domain.repository.AsnRepository;
import com.wms.inbound.domain.repository.ReceiveRepository;
import com.wms.inbound.domain.service.AsnDomainService;
import com.wms.inbound.infrastructure.mapper.AsnHeaderMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiveAppService {
    private final ReceiveRepository receiveRepository;
    private final ReceiveHeaderMapper receiveHeaderMapper;
    private final ReceiveAssembler assembler;
    private final MasterDataGateway masterDataGateway;
    private final AsnDomainService asnDomainService;
    private final AsnRepository asnRepository;
    private final AsnHeaderMapper asnHeaderMapper;

    public IPage<ReceiveDTO> pageReceives(ReceivePageQuery query) {
        LambdaQueryWrapper<ReceiveHeader> wrapper =
                new LambdaQueryWrapper<ReceiveHeader>()
                        .eq(
                                query.getWarehouseId() != null,
                                ReceiveHeader::getWarehouseId,
                                query.getWarehouseId())
                        .eq(
                                query.getOwnerId() != null,
                                ReceiveHeader::getOwnerId,
                                query.getOwnerId())
                        .eq(query.getStatus() != null, ReceiveHeader::getStatus, query.getStatus())
                        .orderByDesc(ReceiveHeader::getCreatedAt);

        if (query.getAsnNo() != null && !query.getAsnNo().isBlank()) {
            List<Long> asnIds =
                    asnHeaderMapper
                            .selectList(
                                    new LambdaQueryWrapper<AsnHeader>()
                                            .like(AsnHeader::getAsnNo, query.getAsnNo())
                                            .select(AsnHeader::getId))
                            .stream()
                            .map(AsnHeader::getId)
                            .toList();
            if (asnIds.isEmpty()) {
                Page<ReceiveHeader> empty = new Page<>(query.getPageNum(), query.getPageSize());
                empty.setTotal(0);
                return empty.convert(assembler::toDTO);
            }
            wrapper.in(ReceiveHeader::getAsnHeaderId, asnIds);
        }

        IPage<ReceiveHeader> result =
                receiveHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return result.convert(assembler::toDTO);
    }

    public ReceiveDTO getReceive(Long id) {
        ReceiveHeader h =
                receiveRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("收货单不存在"));
        ReceiveDTO dto = assembler.toDTO(h);
        if (h.getAsnHeaderId() != null) {
            asnRepository
                    .findById(h.getAsnHeaderId())
                    .ifPresent(asn -> dto.setAsnNo(asn.getAsnNo()));
        }
        if (dto.getAsnNo() == null) {
            dto.setAsnNo("");
        }
        dto.setLines(
                receiveRepository.findLinesByHeader(id).stream()
                        .map(assembler::toLineDTO)
                        .toList());
        return dto;
    }

    @Transactional
    public ReceiveResultDTO receive(ReceiveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String receiveNo =
                "RCV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Sku sku = masterDataGateway.resolveSku(cmd.getSkuId(), cmd.getSkuCode(), tenantId);
        Long locationId = cmd.getReceiveLocationId();
        if (locationId == null && cmd.getReceiveLocationCode() != null) {
            locationId = 0L;
        }

        ReceiveHeader h = new ReceiveHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setOwnerId(cmd.getOwnerId());
        h.setReceiveNo(receiveNo);
        h.setAsnHeaderId(cmd.getAsnHeaderId());
        h.setReceiveType(cmd.getReceiveType() != null ? cmd.getReceiveType() : "ASN");
        h.setStatus(ReceiveHeader.STATUS_DONE);
        h.setReceivedBy(userId);
        h.setReceivedAt(LocalDateTime.now());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        receiveRepository.saveHeader(h);

        ReceiveLine l = new ReceiveLine();
        l.setTenantId(tenantId);
        l.setReceiveHeaderId(h.getId());
        l.setLineNo(1);
        l.setSkuId(sku.getId());
        l.setSkuCode(sku.getSkuCode());
        l.setSkuName(sku.getSkuName());
        l.setReceiveQty(cmd.getReceiveQty());
        l.setReceivePackage(cmd.getReceivePackage());
        l.setReceiveLocationId(locationId);
        l.setAsnLineId(cmd.getAsnLineId());
        l.setBatchNo(cmd.getBatchNo());
        l.setLotAttrs(cmd.getLotAttrs());
        l.setProductionDate(cmd.getProductionDate());
        l.setExpiryDate(cmd.getExpiryDate());
        l.setCreatedBy(userId);
        l.setUpdatedBy(userId);
        receiveRepository.saveLine(l);

        if (h.getAsnHeaderId() != null) {
            asnDomainService.receiveLine(h, l, userId);
        }

        return new ReceiveResultDTO(receiveNo, h.getId());
    }
}
