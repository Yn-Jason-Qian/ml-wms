package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.masterdata.domain.entity.SkuPackage;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class SkuAssembler {
    public Sku toEntity(SkuCreateCmd cmd) {
        Sku s = new Sku();
        s.setOwnerId(cmd.getOwnerId());
        s.setSkuCode(cmd.getSkuCode());
        s.setSkuName(cmd.getSkuName());
        s.setSkuDesc(cmd.getSkuDesc());
        s.setBarcode(cmd.getBarcode());
        s.setCategory(cmd.getCategory());
        s.setBrand(cmd.getBrand());
        s.setSpec(cmd.getSpec());
        s.setBaseUnitId(cmd.getBaseUnitId());
        s.setBaseLength(cmd.getBaseLength());
        s.setBaseWidth(cmd.getBaseWidth());
        s.setBaseHeight(cmd.getBaseHeight());
        s.setBaseWeight(cmd.getBaseWeight());
        s.setBaseVolume(cmd.getBaseVolume());
        s.setShelfLife(cmd.getShelfLife());
        s.setShelfLifeType(cmd.getShelfLifeType());
        s.setBatchManaged(cmd.getBatchManaged());
        s.setSnManaged(cmd.getSnManaged());
        s.setLotAttrs(cmd.getLotAttrs());
        s.setStatus(1);
        return s;
    }

    public void mergeToEntity(SkuUpdateCmd cmd, Sku s) {
        s.setSkuCode(cmd.getSkuCode());
        s.setSkuName(cmd.getSkuName());
        s.setSkuDesc(cmd.getSkuDesc());
        s.setBarcode(cmd.getBarcode());
        s.setCategory(cmd.getCategory());
        s.setBrand(cmd.getBrand());
        s.setSpec(cmd.getSpec());
        s.setBaseUnitId(cmd.getBaseUnitId());
        s.setBaseLength(cmd.getBaseLength());
        s.setBaseWidth(cmd.getBaseWidth());
        s.setBaseHeight(cmd.getBaseHeight());
        s.setBaseWeight(cmd.getBaseWeight());
        s.setBaseVolume(cmd.getBaseVolume());
        s.setShelfLife(cmd.getShelfLife());
        s.setShelfLifeType(cmd.getShelfLifeType());
        s.setBatchManaged(cmd.getBatchManaged());
        s.setSnManaged(cmd.getSnManaged());
        s.setLotAttrs(cmd.getLotAttrs());
    }

    public SkuDTO toDTO(Sku s) {
        SkuDTO dto = new SkuDTO();
        dto.setId(s.getId()); dto.setOwnerId(s.getOwnerId());
        dto.setSkuCode(s.getSkuCode()); dto.setSkuName(s.getSkuName());
        dto.setSkuDesc(s.getSkuDesc()); dto.setBarcode(s.getBarcode());
        dto.setCategory(s.getCategory()); dto.setBrand(s.getBrand());
        dto.setSpec(s.getSpec()); dto.setBaseUnitId(s.getBaseUnitId());
        dto.setBaseLength(s.getBaseLength()); dto.setBaseWidth(s.getBaseWidth());
        dto.setBaseHeight(s.getBaseHeight()); dto.setBaseWeight(s.getBaseWeight());
        dto.setBaseVolume(s.getBaseVolume()); dto.setShelfLife(s.getShelfLife());
        dto.setShelfLifeType(s.getShelfLifeType());
        dto.setBatchManaged(s.getBatchManaged()); dto.setSnManaged(s.getSnManaged());
        dto.setLotAttrs(s.getLotAttrs()); dto.setStatus(s.getStatus());
        dto.setCreatedAt(s.getCreatedAt()); dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }

    // ---- SkuPackage ----

    public SkuPackage toEntity(SkuPackageCreateCmd cmd) {
        SkuPackage p = new SkuPackage();
        p.setSkuId(cmd.getSkuId());
        p.setPackageLevel(cmd.getPackageLevel());
        p.setPackageName(cmd.getPackageName());
        p.setUnitId(cmd.getUnitId());
        p.setQtyPerParent(cmd.getQtyPerParent());
        p.setBarcode(cmd.getBarcode());
        p.setLength(cmd.getLength()); p.setWidth(cmd.getWidth()); p.setHeight(cmd.getHeight());
        p.setWeight(cmd.getWeight());
        p.setIsDefaultReceive(cmd.getIsDefaultReceive() != null ? cmd.getIsDefaultReceive() : 0);
        p.setIsDefaultPick(cmd.getIsDefaultPick() != null ? cmd.getIsDefaultPick() : 0);
        p.setIsDefaultStorage(cmd.getIsDefaultStorage() != null ? cmd.getIsDefaultStorage() : 0);
        return p;
    }

    public SkuPackageDTO toPackageDTO(SkuPackage p) {
        SkuPackageDTO dto = new SkuPackageDTO();
        dto.setId(p.getId()); dto.setSkuId(p.getSkuId());
        dto.setPackageLevel(p.getPackageLevel()); dto.setPackageName(p.getPackageName());
        dto.setUnitId(p.getUnitId()); dto.setQtyPerParent(p.getQtyPerParent());
        dto.setBarcode(p.getBarcode());
        dto.setLength(p.getLength()); dto.setWidth(p.getWidth()); dto.setHeight(p.getHeight());
        dto.setWeight(p.getWeight());
        dto.setIsDefaultReceive(p.getIsDefaultReceive());
        dto.setIsDefaultPick(p.getIsDefaultPick());
        dto.setIsDefaultStorage(p.getIsDefaultStorage());
        return dto;
    }
}
