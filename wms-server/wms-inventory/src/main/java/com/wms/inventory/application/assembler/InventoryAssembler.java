package com.wms.inventory.application.assembler;

import com.wms.inventory.application.dto.*;
import com.wms.inventory.domain.entity.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class InventoryAssembler {

    public StockDTO toDTO(Stock s) {
        if (s == null) return null;
        StockDTO d = new StockDTO();
        d.setId(s.getId()); d.setWarehouseId(s.getWarehouseId()); d.setOwnerId(s.getOwnerId());
        d.setLocationId(s.getLocationId()); d.setSkuId(s.getSkuId());
        d.setSkuCode(s.getSkuCode()); d.setSkuName(s.getSkuName());
        d.setBatchNo(s.getBatchNo()); d.setLotAttrs(s.getLotAttrs());
        d.setProductionDate(s.getProductionDate()); d.setExpiryDate(s.getExpiryDate());
        d.setQtyOnHand(s.getQtyOnHand()); d.setQtyAllocated(s.getQtyAllocated());
        d.setQtyAvailable(s.getQtyAvailable()); d.setQtyFrozen(s.getQtyFrozen());
        d.setStatus(s.getStatus()); d.setFirstInTime(s.getFirstInTime()); d.setLastInTime(s.getLastInTime());
        return d;
    }

    public StockTransactionDTO toDTO(StockTransaction t) {
        if (t == null) return null;
        StockTransactionDTO d = new StockTransactionDTO();
        d.setId(t.getId()); d.setWarehouseId(t.getWarehouseId()); d.setStockId(t.getStockId());
        d.setSkuId(t.getSkuId()); d.setSkuCode(t.getSkuCode()); d.setBatchNo(t.getBatchNo());
        d.setTxnType(t.getTxnType()); d.setTxnDirection(t.getTxnDirection());
        d.setTxnQty(t.getTxnQty()); d.setQtyBefore(t.getQtyBefore()); d.setQtyAfter(t.getQtyAfter());
        d.setRefNo(t.getRefNo()); d.setRefId(t.getRefId());
        d.setFromLocationId(t.getFromLocationId()); d.setToLocationId(t.getToLocationId());
        d.setRemark(t.getRemark()); d.setTxnTime(t.getTxnTime());
        return d;
    }

    public MoveDTO toDTO(MoveHeader h, java.util.List<MoveLine> lines) {
        if (h == null) return null;
        MoveDTO d = new MoveDTO();
        d.setId(h.getId()); d.setWarehouseId(h.getWarehouseId());
        d.setMoveNo(h.getMoveNo()); d.setMoveType(h.getMoveType());
        d.setStatus(h.getStatus()); d.setRemark(h.getRemark());
        d.setCreatedAt(h.getCreatedAt());
        if (lines != null) d.setLines(lines.stream().map(this::toDTO).collect(Collectors.toList()));
        return d;
    }

    public MoveLineDTO toDTO(MoveLine l) {
        if (l == null) return null;
        MoveLineDTO d = new MoveLineDTO();
        d.setId(l.getId()); d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId()); d.setSkuCode(l.getSkuCode()); d.setSkuName(l.getSkuName());
        d.setMoveQty(l.getMoveQty()); d.setFromLocationId(l.getFromLocationId());
        d.setToLocationId(l.getToLocationId()); d.setBatchNo(l.getBatchNo());
        d.setStatus(l.getStatus()); d.setMoveAt(l.getMoveAt());
        return d;
    }

    public FreezeDTO toDTO(Freeze f) {
        if (f == null) return null;
        FreezeDTO d = new FreezeDTO();
        d.setId(f.getId()); d.setWarehouseId(f.getWarehouseId()); d.setFreezeType(f.getFreezeType());
        d.setStockId(f.getStockId()); d.setSkuId(f.getSkuId()); d.setLocationId(f.getLocationId());
        d.setBatchNo(f.getBatchNo()); d.setFreezeQty(f.getFreezeQty());
        d.setReason(f.getReason()); d.setStatus(f.getStatus());
        d.setFreezeBy(f.getFreezeBy()); d.setFreezeAt(f.getFreezeAt()); d.setReleaseAt(f.getReleaseAt());
        return d;
    }

    public StocktakeDTO toDTO(StocktakeHeader h, java.util.List<StocktakeLine> lines) {
        if (h == null) return null;
        StocktakeDTO d = new StocktakeDTO();
        d.setId(h.getId()); d.setWarehouseId(h.getWarehouseId());
        d.setStocktakeNo(h.getStocktakeNo()); d.setStocktakeType(h.getStocktakeType());
        d.setStocktakeMode(h.getStocktakeMode()); d.setStatus(h.getStatus());
        d.setPlanStartTime(h.getPlanStartTime()); d.setPlanEndTime(h.getPlanEndTime());
        d.setStartTime(h.getStartTime()); d.setEndTime(h.getEndTime());
        d.setTotalLines(h.getTotalLines()); d.setDiffLines(h.getDiffLines());
        d.setCreatedAt(h.getCreatedAt());
        if (lines != null) d.setLines(lines.stream().map(this::toDTO).collect(Collectors.toList()));
        return d;
    }

    public StocktakeLineDTO toDTO(StocktakeLine l) {
        if (l == null) return null;
        StocktakeLineDTO d = new StocktakeLineDTO();
        d.setId(l.getId()); d.setLineNo(l.getLineNo());
        d.setLocationId(l.getLocationId()); d.setLocationCode(l.getLocationCode());
        d.setSkuId(l.getSkuId()); d.setSkuCode(l.getSkuCode()); d.setSkuName(l.getSkuName());
        d.setBatchNo(l.getBatchNo()); d.setBookQty(l.getBookQty());
        d.setFirstCountQty(l.getFirstCountQty()); d.setSecondCountQty(l.getSecondCountQty());
        d.setDiffQty(l.getDiffQty()); d.setAdjQty(l.getAdjQty());
        d.setAdjReason(l.getAdjReason()); d.setStatus(l.getStatus());
        return d;
    }
}
