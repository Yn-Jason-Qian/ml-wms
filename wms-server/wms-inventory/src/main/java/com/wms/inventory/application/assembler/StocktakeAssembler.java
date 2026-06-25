package com.wms.inventory.application.assembler;

import com.wms.inventory.application.dto.StocktakeDTO;
import com.wms.inventory.application.dto.StocktakeLineDTO;
import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.domain.entity.StocktakeLine;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StocktakeAssembler {

    public StocktakeDTO toDTO(StocktakeHeader h) {
        return toDTO(h, null);
    }

    public StocktakeDTO toDTO(StocktakeHeader h, List<StocktakeLine> lines) {
        if (h == null) return null;
        StocktakeDTO d = new StocktakeDTO();
        d.setId(h.getId());
        d.setWarehouseId(h.getWarehouseId());
        d.setStocktakeNo(h.getStocktakeNo());
        d.setStocktakeType(h.getStocktakeType());
        d.setStocktakeMode(h.getStocktakeMode());
        d.setStatus(h.getStatus());
        d.setPlanStartTime(h.getPlanStartTime());
        d.setPlanEndTime(h.getPlanEndTime());
        d.setStartTime(h.getStartTime());
        d.setEndTime(h.getEndTime());
        d.setTotalLines(h.getTotalLines());
        d.setDiffLines(h.getDiffLines());
        d.setCreatedAt(h.getCreatedAt());
        if (lines != null) d.setLines(lines.stream().map(this::toDTO).collect(Collectors.toList()));
        return d;
    }

    public StocktakeLineDTO toDTO(StocktakeLine l) {
        if (l == null) return null;
        StocktakeLineDTO d = new StocktakeLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setLocationId(l.getLocationId());
        d.setLocationCode(l.getLocationCode());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setBatchNo(l.getBatchNo());
        d.setBookQty(l.getBookQty());
        d.setFirstCountQty(l.getFirstCountQty());
        d.setSecondCountQty(l.getSecondCountQty());
        d.setDiffQty(l.getDiffQty());
        d.setAdjQty(l.getAdjQty());
        d.setAdjReason(l.getAdjReason());
        d.setStatus(l.getStatus());
        return d;
    }
}
