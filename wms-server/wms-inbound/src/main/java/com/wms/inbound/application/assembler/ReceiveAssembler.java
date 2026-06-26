package com.wms.inbound.application.assembler;

import com.wms.inbound.application.dto.ReceiveDTO;
import com.wms.inbound.application.dto.ReceiveLineDTO;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;

import org.springframework.stereotype.Component;

@Component
public class ReceiveAssembler {

    public ReceiveDTO toDTO(ReceiveHeader h) {
        ReceiveDTO d = new ReceiveDTO();
        d.setId(h.getId());
        d.setReceiveNo(h.getReceiveNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setOwnerId(h.getOwnerId());
        d.setAsnHeaderId(h.getAsnHeaderId());
        d.setReceiveType(h.getReceiveType());
        d.setStatus(h.getStatus());
        d.setRemark(h.getRemark());
        d.setReceivedBy(h.getReceivedBy());
        d.setReceivedAt(h.getReceivedAt());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public ReceiveLineDTO toLineDTO(ReceiveLine l) {
        ReceiveLineDTO d = new ReceiveLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setReceiveQty(l.getReceiveQty());
        d.setReceivePackage(l.getReceivePackage());
        d.setReceiveLocationId(l.getReceiveLocationId());
        d.setBatchNo(l.getBatchNo());
        d.setLotAttrs(l.getLotAttrs());
        d.setProductionDate(l.getProductionDate());
        d.setExpiryDate(l.getExpiryDate());
        return d;
    }
}
