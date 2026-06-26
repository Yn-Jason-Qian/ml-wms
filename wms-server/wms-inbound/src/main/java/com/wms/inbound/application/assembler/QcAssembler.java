package com.wms.inbound.application.assembler;

import com.wms.inbound.application.dto.QcDTO;
import com.wms.inbound.application.dto.QcLineDTO;
import com.wms.inbound.domain.entity.QcHeader;
import com.wms.inbound.domain.entity.QcLine;

import org.springframework.stereotype.Component;

@Component
public class QcAssembler {

    public QcDTO toDTO(QcHeader h) {
        QcDTO d = new QcDTO();
        d.setId(h.getId());
        d.setQcNo(h.getQcNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setReceiveHeaderId(h.getReceiveHeaderId());
        d.setQcType(h.getQcType());
        d.setSampleRatio(h.getSampleRatio());
        d.setStatus(h.getStatus());
        d.setRemark(h.getRemark());
        d.setQcBy(h.getQcBy());
        d.setQcAt(h.getQcAt());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }

    public QcLineDTO toLineDTO(QcLine l) {
        QcLineDTO d = new QcLineDTO();
        d.setId(l.getId());
        d.setLineNo(l.getLineNo());
        d.setSkuId(l.getSkuId());
        d.setSkuCode(l.getSkuCode());
        d.setSkuName(l.getSkuName());
        d.setInspectQty(l.getInspectQty());
        d.setPassQty(l.getPassQty());
        d.setRejectQty(l.getRejectQty());
        d.setRejectReason(l.getRejectReason());
        d.setBatchNo(l.getBatchNo());
        d.setLotAttrs(l.getLotAttrs());
        return d;
    }
}
