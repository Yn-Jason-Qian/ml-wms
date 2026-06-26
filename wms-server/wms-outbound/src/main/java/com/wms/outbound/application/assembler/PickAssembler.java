package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.PickDTO;
import com.wms.outbound.application.dto.PickLineDTO;
import com.wms.outbound.domain.entity.PickHeader;
import com.wms.outbound.domain.entity.PickLine;

import org.springframework.stereotype.Component;

@Component
public class PickAssembler {

    public PickDTO toDTO(PickHeader h) {
        PickDTO d = new PickDTO();
        d.setId(h.getId());
        d.setPickNo(h.getPickNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setPickType(h.getPickType());
        d.setStatus(h.getStatus());
        d.setAssignTo(h.getAssignTo() != null ? h.getAssignTo() : 0L);
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public PickLineDTO toLineDTO(PickLine l) {
        PickLineDTO d = new PickLineDTO();
        d.setId(l.getId());
        d.setPickHeaderId(l.getPickHeaderId());
        d.setLineNo(l.getLineNo());
        d.setOrderHeaderId(l.getOrderHeaderId());
        d.setOrderLineId(l.getOrderLineId());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setPickQty(l.getPickQty());
        d.setPickedQty(l.getPickedQty());
        d.setLocationId(l.getLocationId());
        d.setBatchNo(l.getBatchNo());
        d.setLotAttrs(l.getLotAttrs());
        d.setToContainer(l.getToContainer());
        d.setStatus(l.getStatus());
        return d;
    }
}
