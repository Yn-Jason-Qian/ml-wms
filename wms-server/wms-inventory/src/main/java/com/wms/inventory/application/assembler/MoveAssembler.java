package com.wms.inventory.application.assembler;

import com.wms.inventory.application.dto.MoveDTO;
import com.wms.inventory.application.dto.MoveLineDTO;
import com.wms.inventory.domain.entity.MoveHeader;
import com.wms.inventory.domain.entity.MoveLine;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoveAssembler {

    public MoveDTO toDTO(MoveHeader h) {
        return toDTO(h, null);
    }

    public MoveDTO toDTO(MoveHeader h, List<MoveLine> lines) {
        if (h == null) return null;
        MoveDTO d = new MoveDTO();
        d.setId(h.getId());
        d.setWarehouseId(h.getWarehouseId());
        d.setMoveNo(h.getMoveNo());
        d.setMoveType(h.getMoveType());
        d.setStatus(h.getStatus());
        d.setRemark(h.getRemark());
        d.setCreatedAt(h.getCreatedAt());
        if (lines != null) d.setLines(lines.stream().map(this::toDTO).collect(Collectors.toList()));
        return d;
    }

    public MoveLineDTO toDTO(MoveLine l) {
        if (l == null) return null;
        MoveLineDTO d = new MoveLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setMoveQty(l.getMoveQty());
        d.setFromLocationId(l.getFromLocationId());
        d.setToLocationId(l.getToLocationId());
        d.setBatchNo(l.getBatchNo());
        d.setStatus(l.getStatus());
        d.setMoveAt(l.getMoveAt());
        return d;
    }
}
