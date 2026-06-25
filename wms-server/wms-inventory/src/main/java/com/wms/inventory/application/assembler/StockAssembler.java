package com.wms.inventory.application.assembler;

import com.wms.inventory.application.dto.StockDTO;
import com.wms.inventory.application.dto.StockTransactionDTO;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StockTransaction;

import org.springframework.stereotype.Component;

@Component
public class StockAssembler {

    public StockDTO toDTO(Stock s) {
        if (s == null) return null;
        StockDTO d = new StockDTO();
        d.setId(s.getId());
        d.setWarehouseId(s.getWarehouseId());
        d.setOwnerId(s.getOwnerId());
        d.setLocationId(s.getLocationId());
        d.setSkuId(s.getSkuId());
        d.setSkuCode(s.getSkuCode());
        d.setSkuName(s.getSkuName());
        d.setBatchNo(s.getBatchNo());
        d.setLotAttrs(s.getLotAttrs());
        d.setProductionDate(s.getProductionDate());
        d.setExpiryDate(s.getExpiryDate());
        d.setQtyOnHand(s.getQtyOnHand());
        d.setQtyAllocated(s.getQtyAllocated());
        d.setQtyAvailable(s.getQtyAvailable());
        d.setQtyFrozen(s.getQtyFrozen());
        d.setStatus(s.getStatus());
        d.setFirstInTime(s.getFirstInTime());
        d.setLastInTime(s.getLastInTime());
        return d;
    }

    public StockTransactionDTO toDTO(StockTransaction t) {
        if (t == null) return null;
        StockTransactionDTO d = new StockTransactionDTO();
        d.setId(t.getId());
        d.setWarehouseId(t.getWarehouseId());
        d.setStockId(t.getStockId());
        d.setSkuId(t.getSkuId());
        d.setSkuCode(t.getSkuCode());
        d.setBatchNo(t.getBatchNo());
        d.setTxnType(t.getTxnType());
        d.setTxnDirection(t.getTxnDirection());
        d.setTxnQty(t.getTxnQty());
        d.setQtyBefore(t.getQtyBefore());
        d.setQtyAfter(t.getQtyAfter());
        d.setRefNo(t.getRefNo());
        d.setRefId(t.getRefId());
        d.setFromLocationId(t.getFromLocationId());
        d.setToLocationId(t.getToLocationId());
        d.setRemark(t.getRemark());
        d.setTxnTime(t.getTxnTime());
        return d;
    }
}
