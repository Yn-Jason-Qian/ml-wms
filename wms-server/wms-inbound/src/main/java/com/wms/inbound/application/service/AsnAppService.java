package com.wms.inbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inbound.application.assembler.AsnAssembler;
import com.wms.inbound.application.dto.AsnCreateCmd;
import com.wms.inbound.application.dto.AsnDTO;
import com.wms.inbound.application.dto.AsnPageQuery;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.AsnLine;
import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.inbound.domain.repository.AsnRepository;
import com.wms.inbound.infrastructure.mapper.AsnHeaderMapper;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AsnAppService {
    private final AsnRepository asnRepository;
    private final AsnHeaderMapper asnHeaderMapper;
    private final MasterDataGateway masterDataGateway;
    private final AsnAssembler assembler;

    @Transactional
    public AsnDTO createAsn(AsnCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String asnNo =
                "ASN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        AsnHeader h = new AsnHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setOwnerId(cmd.getOwnerId());
        h.setAsnNo(asnNo);
        h.setAsnType(cmd.getAsnType());
        h.setSourceNo(cmd.getSourceNo());
        h.setExpectedArriveTime(cmd.getExpectedArriveTime());
        h.setCarrierName(cmd.getCarrierName());
        h.setCarrierPhone(cmd.getCarrierPhone());
        h.setRemark(cmd.getRemark());
        h.setStatus(AsnHeader.Status.CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        asnRepository.saveHeader(h);

        int lineNo = 0;
        for (AsnCreateCmd.AsnLineItem item : cmd.getLines()) {
            lineNo++;
            Sku sku = masterDataGateway.resolveSku(item.getSkuId(), item.getSkuCode(), tenantId);
            AsnLine l = new AsnLine();
            l.setTenantId(tenantId);
            l.setAsnHeaderId(h.getId());
            l.setLineNo(lineNo);
            l.setSkuId(sku.getId());
            l.setSkuCode(sku.getSkuCode());
            l.setSkuName(sku.getSkuName());
            l.setExpectedQty(item.getExpectedQty());
            l.setReceivedQty(BigDecimal.ZERO);
            l.setBatchNo(item.getBatchNo());
            l.setLotAttrs(item.getLotAttrs());
            l.setProductionDate(item.getProductionDate());
            l.setExpiryDate(item.getExpiryDate());
            l.setStatus("CREATED");
            l.setCreatedBy(userId);
            l.setUpdatedBy(userId);
            asnRepository.saveLine(l);
        }

        AsnDTO dto = assembler.toDTO(h);
        dto.setLines(
                asnRepository.findLinesByHeader(h.getId()).stream()
                        .map(assembler::toLineDTO)
                        .toList());
        return dto;
    }

    public AsnDTO getAsn(Long id) {
        AsnHeader h =
                asnRepository.findById(id).orElseThrow(() -> BusinessException.notFound("ASN不存在"));
        AsnDTO dto = assembler.toDTO(h);
        dto.setLines(
                asnRepository.findLinesByHeader(id).stream().map(assembler::toLineDTO).toList());
        return dto;
    }

    public IPage<AsnDTO> pageAsns(AsnPageQuery query) {
        IPage<AsnHeader> result =
                asnHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<AsnHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        AsnHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getOwnerId() != null,
                                        AsnHeader::getOwnerId,
                                        query.getOwnerId())
                                .eq(
                                        query.getAsnType() != null,
                                        AsnHeader::getAsnType,
                                        query.getAsnType())
                                .eq(
                                        query.getStatus() != null,
                                        AsnHeader::getStatus,
                                        query.getStatus())
                                .like(
                                        query.getAsnNo() != null,
                                        AsnHeader::getAsnNo,
                                        query.getAsnNo())
                                .orderByDesc(AsnHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public AsnDTO getAsnByNo(String asnNo) {
        AsnHeader h =
                asnHeaderMapper.selectOne(
                        new LambdaQueryWrapper<AsnHeader>()
                                .eq(AsnHeader::getAsnNo, asnNo)
                                .eq(AsnHeader::getTenantId, UserContext.getTenantId()));
        if (h == null) {
            return null;
        }
        AsnDTO dto = assembler.toDTO(h);
        dto.setLines(
                asnRepository.findLinesByHeader(h.getId()).stream()
                        .map(assembler::toLineDTO)
                        .toList());
        return dto;
    }
}
