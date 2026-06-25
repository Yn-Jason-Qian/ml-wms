package com.wms.inbound.application.assembler;

import com.wms.inbound.application.dto.PutawayDTO;
import com.wms.inbound.application.dto.PutawayLineDTO;
import com.wms.inbound.domain.entity.PutawayHeader;
import com.wms.inbound.domain.entity.PutawayLine;

import org.springframework.stereotype.Component;

@Component
public class PutawayAssembler {

    public PutawayDTO toDTO(PutawayHeader h) {
        PutawayDTO d = new PutawayDTO();
        d.setId(h.getId());
        d.setPutawayNo(h.getPutawayNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setReceiveHeaderId(h.getReceiveHeaderId());
        d.setStatus(h.getStatus());
        d.setStrategyId(h.getStrategyId());
        d.setRemark(h.getRemark());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public PutawayLineDTO toLineDTO(PutawayLine l) {
        PutawayLineDTO d = new PutawayLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setPutawayQty(l.getPutawayQty());
        d.setDoneQty(l.getDoneQty());
        d.setFromLocationId(l.getFromLocationId());
        d.setToLocationId(l.getToLocationId());
        d.setBatchNo(l.getBatchNo());
        d.setLotAttrs(l.getLotAttrs());
        d.setStatus(l.getStatus());
        d.setPutawayAt(l.getPutawayAt());
        return d;
    }
}
