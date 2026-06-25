package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.ShipDTO;
import com.wms.outbound.domain.entity.ShipHeader;

import org.springframework.stereotype.Component;

@Component
public class ShipAssembler {

    public ShipDTO toDTO(ShipHeader h) {
        ShipDTO d = new ShipDTO();
        d.setId(h.getId());
        d.setShipNo(h.getShipNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setDeliveryMethod(h.getDeliveryMethod());
        d.setCarrierName(h.getCarrierName() != null ? h.getCarrierName() : "");
        d.setTrackingNo(h.getTrackingNo() != null ? h.getTrackingNo() : "");
        d.setStatus(h.getStatus());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }
}
