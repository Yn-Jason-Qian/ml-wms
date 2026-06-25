package com.wms.inbound.application.service;

import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.domain.entity.*;
import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.inbound.domain.repository.InboundRepository;
import com.wms.inbound.domain.service.InboundDomainService;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundAppService {
    private final InboundRepository inboundRepo;
    private final InboundDomainService domainService;
    private final MasterDataGateway masterDataGateway;

    // ───── ASN ─────

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
        h.setStatus(AsnHeader.STATUS_CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        inboundRepo.saveAsnHeader(h);

        int lineNo = 0;
        for (AsnCreateCmd.AsnLineItem item : cmd.getLines()) {
            lineNo++;
            // 支持 SKU code → ID 查找 (PDA扫码场景传 code)
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
            inboundRepo.saveAsnLine(l);
        }

        return toAsnDTO(h, inboundRepo.findAsnLinesByHeader(h.getId()));
    }

    public AsnDTO getAsn(Long id) {
        AsnHeader h =
                inboundRepo
                        .findAsnHeaderById(id)
                        .orElseThrow(() -> BusinessException.notFound("ASN不存在"));
        return toAsnDTO(h, inboundRepo.findAsnLinesByHeader(id));
    }

    // ───── 收货 ─────

    @Transactional
    public Object receive(ReceiveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String receiveNo =
                "RCV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 支持 SKU code → ID 查找
        Sku sku = masterDataGateway.resolveSku(cmd.getSkuId(), cmd.getSkuCode(), tenantId);
        // 支持库位 code → ID 查找 (如果传了 code 但未传 id, 使用 code 记录)
        Long locationId = cmd.getReceiveLocationId();
        if (locationId == null && cmd.getReceiveLocationCode() != null) {
            locationId = 0L; // 后续可通过 location 仓库实现 code→ID 查找
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
        inboundRepo.saveReceiveHeader(h);

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
        inboundRepo.saveReceiveLine(l);

        // 如果关联ASN，执行收货确认
        if (h.getAsnHeaderId() != null) {
            domainService.receiveLine(h, l, userId);
        }

        return java.util.Map.of("receiveNo", receiveNo, "receiveHeaderId", h.getId());
    }

    // ───── 质检 ─────

    @Transactional
    public Object createQc(QcCreateCmd cmd) {
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
        h.setStatus(QcHeader.STATUS_CREATED);
        h.setRemark(cmd.getRemark());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        inboundRepo.saveQcHeader(h);
        return java.util.Map.of("qcNo", qcNo, "qcHeaderId", h.getId());
    }

    @Transactional
    public void submitQcResult(QcSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        QcHeader h =
                inboundRepo
                        .findQcHeaderById(cmd.getHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("质检单不存在"));

        QcLine l = new QcLine();
        l.setTenantId(UserContext.getTenantId());
        l.setQcHeaderId(cmd.getHeaderId());
        l.setLineNo(1);
        l.setSkuId(cmd.getSkuId());

        // 补齐 SKU 编码/名称（数据库字段 NOT NULL）
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
        inboundRepo.saveQcLine(l);

        // 判定：有不合格 → REJECT，否则 PASS
        h.setStatus(
                cmd.getRejectQty() != null && cmd.getRejectQty().compareTo(BigDecimal.ZERO) > 0
                        ? QcHeader.STATUS_REJECT
                        : QcHeader.STATUS_PASS);
        h.setQcBy(userId);
        h.setQcAt(LocalDateTime.now());
        inboundRepo.updateQcHeader(h);
    }

    // ───── 上架 ─────

    @Transactional
    public Object createPutaway(PutawayCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String paNo =
                "PA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 从收货行获取待上架明细
        ReceiveHeader rcv =
                inboundRepo
                        .findReceiveHeaderById(cmd.getReceiveHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("收货单不存在"));
        List<ReceiveLine> rcvLines = inboundRepo.findReceiveLinesByHeader(rcv.getId());

        PutawayHeader h = new PutawayHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setPutawayNo(paNo);
        h.setReceiveHeaderId(cmd.getReceiveHeaderId());
        h.setStatus(PutawayHeader.STATUS_CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        inboundRepo.savePutawayHeader(h);

        int lineNo = 0;
        for (ReceiveLine rl : rcvLines) {
            lineNo++;
            PutawayLine pl = new PutawayLine();
            pl.setTenantId(tenantId);
            pl.setPutawayHeaderId(h.getId());
            pl.setLineNo(lineNo);
            pl.setSkuId(rl.getSkuId());
            pl.setSkuCode(rl.getSkuCode());
            pl.setSkuName(rl.getSkuName());
            pl.setPutawayQty(rl.getReceiveQty());
            pl.setDoneQty(BigDecimal.ZERO);
            pl.setFromLocationId(rl.getReceiveLocationId());
            pl.setBatchNo(rl.getBatchNo());
            pl.setLotAttrs(rl.getLotAttrs());
            pl.setStatus("CREATED");
            pl.setToLocationId(0L);
            pl.setCreatedBy(userId);
            pl.setUpdatedBy(userId);
            inboundRepo.savePutawayLine(pl);
        }

        h.setStatus(PutawayHeader.STATUS_PUTAWAYING);
        inboundRepo.updatePutawayHeader(h);
        return java.util.Map.of("putawayNo", paNo, "putawayHeaderId", h.getId());
    }

    @Transactional
    public void submitPutaway(PutawaySubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        PutawayHeader paHeader =
                inboundRepo
                        .findPutawayHeaderById(cmd.getPutawayHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("上架单不存在"));
        PutawayLine line =
                inboundRepo.findPutawayLinesByHeader(paHeader.getId()).stream()
                        .filter(l -> l.getId().equals(cmd.getPutawayLineId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("上架行不存在"));

        if (cmd.getToLocationId() != null) {
            line.setToLocationId(cmd.getToLocationId());
        }

        // 获取 warehouseId 和 ownerId
        Long warehouseId = paHeader.getWarehouseId();
        Long ownerId =
                inboundRepo
                        .findReceiveHeaderById(paHeader.getReceiveHeaderId())
                        .map(ReceiveHeader::getOwnerId)
                        .orElse(0L);

        // 执行上架 + 增加库存
        domainService.executePutaway(line, warehouseId, ownerId, userId);

        // 更新上架行状态
        line.setDoneQty(line.getPutawayQty());
        line.setStatus("DONE");
        line.setPutawayBy(userId);
        line.setPutawayAt(LocalDateTime.now());
        line.setUpdatedBy(userId);
        inboundRepo.updatePutawayLine(line);

        // 检查是否全部上架完成
        List<PutawayLine> lines = inboundRepo.findPutawayLinesByHeader(cmd.getPutawayHeaderId());
        boolean allDone = lines.stream().allMatch(l -> "DONE".equals(l.getStatus()));
        if (allDone) {
            PutawayHeader h =
                    inboundRepo.findPutawayHeaderById(cmd.getPutawayHeaderId()).orElse(null);
            if (h != null) {
                h.setStatus(PutawayHeader.STATUS_DONE);
                inboundRepo.updatePutawayHeader(h);
            }
        }
    }

    // ───── DTO ─────

    private AsnDTO toAsnDTO(AsnHeader h, List<AsnLine> lines) {
        AsnDTO d = new AsnDTO();
        d.setId(h.getId());
        d.setWarehouseId(h.getWarehouseId());
        d.setOwnerId(h.getOwnerId());
        d.setAsnNo(h.getAsnNo());
        d.setAsnType(h.getAsnType());
        d.setSourceNo(h.getSourceNo());
        d.setExpectedArriveTime(h.getExpectedArriveTime());
        d.setCarrierName(h.getCarrierName());
        d.setStatus(h.getStatus());
        d.setRemark(h.getRemark());
        d.setCreatedAt(h.getCreatedAt());
        d.setLines(
                lines.stream()
                        .map(
                                l -> {
                                    AsnLineDTO ld = new AsnLineDTO();
                                    ld.setId(l.getId());
                                    ld.setLineNo(l.getLineNo());
                                    ld.setSkuId(l.getSkuId());
                                    ld.setSkuCode(l.getSkuCode());
                                    ld.setSkuName(l.getSkuName());
                                    ld.setExpectedQty(l.getExpectedQty());
                                    ld.setReceivedQty(l.getReceivedQty());
                                    ld.setRemainingQty(l.getRemainingQty());
                                    ld.setBatchNo(l.getBatchNo());
                                    ld.setLotAttrs(l.getLotAttrs());
                                    ld.setProductionDate(l.getProductionDate());
                                    ld.setExpiryDate(l.getExpiryDate());
                                    ld.setStatus(l.getStatus());
                                    return ld;
                                })
                        .collect(Collectors.toList()));
        return d;
    }
}
