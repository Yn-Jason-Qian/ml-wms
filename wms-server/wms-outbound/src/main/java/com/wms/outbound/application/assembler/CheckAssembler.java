package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.CheckDTO;
import com.wms.outbound.application.dto.CheckLineDTO;
import com.wms.outbound.domain.entity.CheckHeader;
import com.wms.outbound.domain.entity.CheckLine;

import org.springframework.stereotype.Component;

@Component
public class CheckAssembler {

    public CheckDTO toDTO(CheckHeader h) {
        CheckDTO d = new CheckDTO();
        d.setId(h.getId());
        d.setCheckNo(h.getCheckNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setWaveHeaderId(h.getWaveHeaderId());
        d.setStatus(h.getStatus());
        d.setCheckBy(h.getCheckBy());
        d.setStartTime(h.getStartTime());
        d.setEndTime(h.getEndTime());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public CheckLineDTO toLineDTO(CheckLine l) {
        CheckLineDTO d = new CheckLineDTO();
        d.setId(l.getId());
        d.setCheckHeaderId(l.getCheckHeaderId());
        d.setLineNo(l.getLineNo());
        d.setOrderHeaderId(l.getOrderHeaderId());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setOrderQty(l.getOrderQty());
        d.setCheckQty(l.getCheckQty());
        d.setIsMatch(l.getIsMatch());
        d.setDiffReason(l.getDiffReason());
        d.setFromContainer(l.getFromContainer());
        d.setToContainer(l.getToContainer());
        return d;
    }
}
