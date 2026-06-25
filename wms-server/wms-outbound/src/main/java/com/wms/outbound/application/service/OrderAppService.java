package com.wms.outbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.outbound.application.assembler.OrderAssembler;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.OrderHeader;
import com.wms.outbound.domain.entity.OrderLine;
import com.wms.outbound.domain.gateway.MasterDataGateway;
import com.wms.outbound.domain.repository.OrderRepository;
import com.wms.outbound.domain.service.OrderDomainService;
import com.wms.outbound.infrastructure.mapper.OrderHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderAppService {
    private final OrderRepository orderRepository;
    private final OrderHeaderMapper orderMapper;
    private final MasterDataGateway masterDataGateway;
    private final OrderDomainService orderDomainService;
    private final OrderAssembler assembler;

    public IPage<OrderDTO> pageOrders(OrderPageQuery query) {
        IPage<OrderHeader> result =
                orderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<OrderHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        OrderHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        OrderHeader::getStatus,
                                        query.getStatus())
                                .eq(
                                        query.getOrderType() != null,
                                        OrderHeader::getOrderType,
                                        query.getOrderType())
                                .orderByDesc(OrderHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public OrderDTO getOrder(Long id) {
        OrderHeader h =
                orderRepository.findById(id).orElseThrow(() -> BusinessException.notFound("订单不存在"));
        OrderDTO dto = assembler.toDTO(h);
        dto.setLines(orderRepository.findLines(id).stream().map(assembler::toLineDTO).toList());
        return dto;
    }

    @Transactional
    public OrderResultDTO createOrder(OrderCreateCmd cmd) {
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
        orderRepository.save(h);

        int lineNo = 0;
        for (OrderCreateCmd.LineItem item : cmd.getLines()) {
            lineNo++;
            Sku sku = masterDataGateway.resolveSku(item.getSkuId(), item.getSkuCode(), tenantId);
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
            orderRepository.saveLine(l);

            orderDomainService.allocateInventory(l, tenantId, cmd.getWarehouseId());
            l.setAllocatedQty(l.getOrderQty());
            l.setStatus("ALLOCATED");
            orderRepository.updateLine(l);
        }
        h.setStatus(OrderHeader.STATUS_ALLOCATED);
        orderRepository.update(h);
        return new OrderResultDTO(orderNo, h.getId());
    }
}
