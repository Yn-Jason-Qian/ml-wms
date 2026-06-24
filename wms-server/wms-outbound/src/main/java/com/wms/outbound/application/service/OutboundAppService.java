package com.wms.outbound.application.service;

import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.masterdata.domain.repository.SkuRepository;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.OutboundRepository;
import com.wms.outbound.domain.service.OutboundDomainService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboundAppService {
    private final OutboundRepository outboundRepo;
    private final OutboundDomainService domainService;
    private final SkuRepository skuRepository;

    // ───── 订单 ─────

    @Transactional
    public Map<String, Object> createOrder(OrderCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String orderNo =
                "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        OrderHeader h = new OrderHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setOwnerId(cmd.getOwnerId());
        h.setOrderNo(orderNo);
        h.setOrderType(cmd.getOrderType());
        h.setSourceNo(cmd.getSourceNo());
        h.setCustomerName(cmd.getCustomerName());
        h.setCustomerAddress(cmd.getCustomerAddress());
        h.setExpectedShipTime(cmd.getExpectedShipTime());
        h.setPriority(cmd.getPriority() != null ? cmd.getPriority() : 5);
        h.setStatus(OrderHeader.STATUS_CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        outboundRepo.saveOrder(h);

        int lineNo = 0;
        for (OrderCreateCmd.LineItem item : cmd.getLines()) {
            lineNo++;
            // 支持 SKU code → ID 查找
            Sku sku = resolveSku(item.getSkuId(), item.getSkuCode(), tenantId);
            OrderLine l = new OrderLine();
            l.setTenantId(tenantId);
            l.setOrderHeaderId(h.getId());
            l.setLineNo(lineNo);
            l.setSkuId(sku.getId());
            l.setSkuCode(sku.getSkuCode());
            l.setSkuName(sku.getSkuName());
            l.setOrderQty(item.getOrderQty());
            l.setAllocatedQty(BigDecimal.ZERO);
            l.setPickedQty(BigDecimal.ZERO);
            l.setShippedQty(BigDecimal.ZERO);
            l.setBatchNo(item.getBatchNo());
            l.setLotAttrs(item.getLotAttrs());
            l.setStatus("CREATED");
            l.setCreatedBy(userId);
            l.setUpdatedBy(userId);
            outboundRepo.saveOrderLine(l);

            // 自动分配库存
            domainService.allocateInventory(l, tenantId, cmd.getWarehouseId());
            l.setAllocatedQty(l.getOrderQty());
        }
        h.setStatus(OrderHeader.STATUS_ALLOCATED);
        outboundRepo.updateOrder(h);
        return Map.of("orderNo", orderNo, "orderId", h.getId());
    }

    // ───── 波次 ─────

    @Transactional
    public Map<String, Object> createWave(WaveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String waveNo =
                "WAV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        WaveHeader h = new WaveHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setWaveNo(waveNo);
        h.setWaveType(cmd.getWaveType());
        h.setWaveStatus(WaveHeader.STATUS_CREATED);
        h.setOrderCount(cmd.getOrderIds().size());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        outboundRepo.saveWave(h);

        int sort = 0;
        for (Long orderId : cmd.getOrderIds()) {
            WaveLine wl = new WaveLine();
            wl.setTenantId(tenantId);
            wl.setWaveHeaderId(h.getId());
            wl.setOrderHeaderId(orderId);
            wl.setSortOrder(++sort);
            wl.setCreatedBy(userId);
            wl.setUpdatedBy(userId);
            outboundRepo.saveWaveLine(wl);

            // 更新订单关联波次
            OrderHeader order = outboundRepo.findOrderById(orderId).orElse(null);
            if (order != null) {
                order.setWaveHeaderId(h.getId());
                outboundRepo.updateOrder(order);
            }
        }

        h.release();
        h.setReleasedBy(userId);
        outboundRepo.updateWave(h);
        return Map.of("waveNo", waveNo, "waveId", h.getId());
    }

    // ───── 拣货 ─────

    @Transactional
    public Map<String, Object> createPickForWave(Long waveHeaderId) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String pickNo =
                "PICK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        WaveHeader wave =
                outboundRepo
                        .findWaveById(waveHeaderId)
                        .orElseThrow(() -> BusinessException.notFound("波次不存在"));

        PickHeader h = new PickHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(wave.getWarehouseId());
        h.setPickNo(pickNo);
        h.setWaveHeaderId(waveHeaderId);
        h.setPickType("RF");
        h.setStatus(PickHeader.STATUS_CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        outboundRepo.savePickHeader(h);

        // 从波次关联的订单创建拣货行
        int lineNo = 0;
        for (WaveLine wl : outboundRepo.findWaveLines(waveHeaderId)) {
            for (OrderLine ol : outboundRepo.findOrderLines(wl.getOrderHeaderId())) {
                lineNo++;
                PickLine pl = new PickLine();
                pl.setTenantId(tenantId);
                pl.setPickHeaderId(h.getId());
                pl.setLineNo(lineNo);
                pl.setOrderHeaderId(ol.getOrderHeaderId());
                pl.setOrderLineId(ol.getId());
                pl.setSkuId(ol.getSkuId());
                pl.setSkuCode(ol.getSkuCode());
                pl.setSkuName(ol.getSkuName());
                pl.setPickQty(ol.getOrderQty());
                pl.setPickedQty(BigDecimal.ZERO);
                pl.setBatchNo(ol.getBatchNo());
                pl.setLotAttrs(ol.getLotAttrs());
                pl.setStatus("CREATED");
                pl.setCreatedBy(userId);
                pl.setUpdatedBy(userId);
                outboundRepo.savePickLine(pl);
            }
        }
        return Map.of("pickNo", pickNo, "pickId", h.getId());
    }

    @Transactional
    public void submitPick(PickSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        PickHeader h =
                outboundRepo
                        .findPickById(cmd.getPickHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("拣货单不存在"));
        PickLine l =
                outboundRepo.findPickLines(h.getId()).stream()
                        .filter(p -> p.getId().equals(cmd.getPickLineId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("拣货行不存在"));

        l.setPickedQty(cmd.getPickedQty());
        l.setToContainer(cmd.getToContainer());
        l.setStatus("PICKED");
        l.setPickBy(userId);
        l.setPickAt(LocalDateTime.now());
        l.setUpdatedBy(userId);
        outboundRepo.updatePickLine(l);

        boolean allDone =
                outboundRepo.findPickLines(h.getId()).stream()
                        .allMatch(p -> "PICKED".equals(p.getStatus()));
        if (allDone) {
            h.setStatus(PickHeader.STATUS_PICKED);
            outboundRepo.updatePickHeader(h);
        }
    }

    // ───── 发货 ─────

    @Transactional
    public Map<String, Object> createShip(ShipCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String shipNo =
                "SHP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        ShipHeader h = new ShipHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setOwnerId(cmd.getOwnerId());
        h.setShipNo(shipNo);
        h.setWaveHeaderId(cmd.getWaveHeaderId());
        h.setDeliveryMethod(cmd.getDeliveryMethod());
        h.setCarrierName(cmd.getCarrierName());
        h.setTrackingNo(cmd.getTrackingNo());
        h.setPackageCount(cmd.getPackageCount());
        h.setGrossWeight(cmd.getGrossWeight());
        h.setVolume(cmd.getVolume());
        h.setStatus(ShipHeader.STATUS_DONE);
        h.setShipBy(userId);
        h.setShipAt(LocalDateTime.now());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        outboundRepo.saveShipHeader(h);

        // 扣减库存
        WaveHeader wave = outboundRepo.findWaveById(cmd.getWaveHeaderId()).orElse(null);
        if (wave != null) {
            for (WaveLine wl : outboundRepo.findWaveLines(wave.getId())) {
                PickHeader pickH = findPickByWave(wave.getId());
                if (pickH != null) {
                    for (PickLine pl : outboundRepo.findPickLines(pickH.getId())) {
                        if ("PICKED".equals(pl.getStatus())) {
                            domainService.shipDeduct(pl, userId);
                        }
                    }
                }
            }
        }
        return Map.of("shipNo", shipNo, "shipId", h.getId());
    }

    private PickHeader findPickByWave(Long waveId) {
        // 查找关联的拣货单 (简化: 返回最新创建的)
        return outboundRepo.findPickByWave(waveId).orElse(null);
    }

    /** 根据 ID 或 code 查找 SKU */
    private Sku resolveSku(Long skuId, String skuCode, Long tenantId) {
        if (skuId != null) {
            return skuRepository
                    .findById(skuId)
                    .orElseThrow(() -> BusinessException.notFound("SKU不存在: id=" + skuId));
        }
        if (skuCode != null && !skuCode.isBlank()) {
            return skuRepository
                    .findByCode(tenantId, skuCode)
                    .orElseThrow(() -> BusinessException.notFound("SKU不存在: code=" + skuCode));
        }
        throw BusinessException.badRequest("skuId 或 skuCode 必须提供一个");
    }
}
