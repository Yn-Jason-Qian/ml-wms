package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.OrderDTO;
import com.wms.outbound.application.dto.OrderLineDTO;
import com.wms.outbound.application.dto.WaveOrderDTO;
import com.wms.outbound.domain.entity.OrderHeader;
import com.wms.outbound.domain.entity.OrderLine;

import org.springframework.stereotype.Component;

@Component
public class OrderAssembler {

    public OrderDTO toDTO(OrderHeader h) {
        OrderDTO d = new OrderDTO();
        d.setId(h.getId());
        d.setOrderNo(h.getOrderNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setOwnerId(h.getOwnerId());
        d.setOrderType(h.getOrderType());
        d.setCustomerName(h.getCustomerName() != null ? h.getCustomerName() : "");
        d.setCustomerAddress(h.getCustomerAddress() != null ? h.getCustomerAddress() : "");
        d.setPriority(h.getPriority());
        d.setStatus(h.getStatus());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public OrderLineDTO toLineDTO(OrderLine l) {
        OrderLineDTO d = new OrderLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setOrderQty(l.getOrderQty());
        d.setAllocatedQty(l.getAllocatedQty());
        d.setPickedQty(l.getPickedQty());
        d.setShippedQty(l.getShippedQty());
        d.setBatchNo(l.getBatchNo() != null ? l.getBatchNo() : "");
        d.setStatus(l.getStatus());
        return d;
    }

    public WaveOrderDTO toOrderRefDTO(OrderHeader o) {
        WaveOrderDTO d = new WaveOrderDTO();
        d.setId(o.getId());
        d.setOrderNo(o.getOrderNo());
        d.setOrderType(o.getOrderType());
        d.setCustomerName(o.getCustomerName() != null ? o.getCustomerName() : "");
        d.setPriority(o.getPriority());
        d.setStatus(o.getStatus());
        return d;
    }
}
