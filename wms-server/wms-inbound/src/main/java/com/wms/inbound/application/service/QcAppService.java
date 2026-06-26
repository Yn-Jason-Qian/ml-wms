package com.wms.inbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inbound.application.assembler.QcAssembler;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.domain.entity.QcHeader;
import com.wms.inbound.domain.entity.QcLine;
import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.inbound.domain.repository.QcRepository;
import com.wms.inbound.domain.repository.ReceiveRepository;
import com.wms.inbound.infrastructure.mapper.QcHeaderMapper;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QcAppService {
    private final QcRepository qcRepository;
    private final QcHeaderMapper qcHeaderMapper;
    private final QcAssembler assembler;
    private final MasterDataGateway masterDataGateway;
    private final ReceiveRepository receiveRepository;

    public IPage<QcDTO> pageQcs(QcPageQuery query) {
        IPage<QcHeader> result =
                qcHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<QcHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        QcHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        QcHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(QcHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public QcDTO getQc(Long id) {
        QcHeader h =
                qcRepository.findById(id).orElseThrow(() -> BusinessException.notFound("质检单不存在"));
        QcDTO dto = assembler.toDTO(h);
        if (h.getReceiveHeaderId() != null) {
            receiveRepository
                    .findById(h.getReceiveHeaderId())
                    .ifPresent(
                            rcv -> {
                                dto.setReceiveNo(rcv.getReceiveNo());
                                dto.setOwnerId(rcv.getOwnerId());
                            });
        }
        if (dto.getReceiveNo() == null) {
            dto.setReceiveNo("");
        }
        dto.setLines(
                qcRepository.findLinesByHeader(id).stream().map(assembler::toLineDTO).toList());
        return dto;
    }

    @Transactional
    public QcResultDTO createQc(QcCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String qcNo =
                "QC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        QcHeader h = new QcHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setQcNo(qcNo);
        h.setReceiveHeaderId(cmd.getReceiveHeaderId());
        h.setQcType(cmd.getQcType());
        h.setSampleRatio(cmd.getSampleRatio());
        h.setStatus(QcHeader.Status.CREATED);
        h.setRemark(cmd.getRemark());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        qcRepository.saveHeader(h);
        return new QcResultDTO(qcNo, h.getId());
    }

    @Transactional
    public void submitQcResult(QcSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        QcHeader h =
                qcRepository
                        .findById(cmd.getHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("质检单不存在"));

        QcLine l = new QcLine();
        l.setTenantId(UserContext.getTenantId());
        l.setQcHeaderId(cmd.getHeaderId());
        l.setLineNo(1);
        l.setSkuId(cmd.getSkuId());

        Sku sku = masterDataGateway.resolveSku(cmd.getSkuId(), null, UserContext.getTenantId());
        if (sku != null) {
            l.setSkuCode(sku.getSkuCode());
            l.setSkuName(sku.getSkuName());
        } else {
            l.setSkuCode("");
            l.setSkuName("");
        }

        l.setInspectQty(cmd.getInspectQty());
        l.setPassQty(cmd.getPassQty());
        l.setRejectQty(cmd.getRejectQty() != null ? cmd.getRejectQty() : BigDecimal.ZERO);
        l.setRejectReason(cmd.getRejectReason());
        l.setBatchNo(cmd.getBatchNo());
        l.setCreatedBy(userId);
        l.setUpdatedBy(userId);
        qcRepository.saveLine(l);

        h.setStatus(
                cmd.getRejectQty() != null && cmd.getRejectQty().compareTo(BigDecimal.ZERO) > 0
                        ? QcHeader.Status.REJECT
                        : QcHeader.Status.PASS);
        h.setQcBy(userId);
        h.setQcAt(LocalDateTime.now());
        qcRepository.updateHeader(h);
    }
}
