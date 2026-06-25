package com.wms.inventory.application.assembler;

import com.wms.inventory.application.dto.FreezeDTO;
import com.wms.inventory.domain.entity.Freeze;

import org.springframework.stereotype.Component;

@Component
public class FreezeAssembler {

    public FreezeDTO toDTO(Freeze f) {
        if (f == null) return null;
        FreezeDTO d = new FreezeDTO();
        d.setId(f.getId());
        d.setWarehouseId(f.getWarehouseId());
        d.setFreezeType(f.getFreezeType());
        d.setStockId(f.getStockId());
        d.setSkuId(f.getSkuId());
        d.setLocationId(f.getLocationId());
        d.setBatchNo(f.getBatchNo());
        d.setFreezeQty(f.getFreezeQty());
        d.setReason(f.getReason());
        d.setStatus(f.getStatus());
        d.setFreezeBy(f.getFreezeBy());
        d.setFreezeAt(f.getFreezeAt());
        d.setReleaseAt(f.getReleaseAt());
        return d;
    }
}
