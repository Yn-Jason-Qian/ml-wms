package com.wms.inbound.application.assembler;

import com.wms.inbound.application.dto.AsnDTO;
import com.wms.inbound.application.dto.AsnLineDTO;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.AsnLine;

import org.springframework.stereotype.Component;

@Component
public class AsnAssembler {

    public AsnDTO toDTO(AsnHeader h) {
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
        return d;
    }

    public AsnLineDTO toLineDTO(AsnLine l) {
        AsnLineDTO d = new AsnLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setExpectedQty(l.getExpectedQty());
        d.setReceivedQty(l.getReceivedQty());
        d.setRemainingQty(l.getRemainingQty());
        d.setBatchNo(l.getBatchNo());
        d.setLotAttrs(l.getLotAttrs());
        d.setProductionDate(l.getProductionDate());
        d.setExpiryDate(l.getExpiryDate());
        d.setStatus(l.getStatus());
        return d;
    }
}
