package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.WarehouseCreateCmd;
import com.wms.masterdata.application.dto.WarehouseDTO;
import com.wms.masterdata.application.dto.WarehouseUpdateCmd;
import com.wms.masterdata.domain.entity.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseAssembler {

    /** CreateCmd → 领域实体 */
    public Warehouse toEntity(WarehouseCreateCmd cmd) {
        Warehouse w = new Warehouse();
        w.setWhCode(cmd.getWhCode());
        w.setWhName(cmd.getWhName());
        w.setWhType(cmd.getWhType());
        w.setAddress(cmd.getAddress());
        w.setContactPerson(cmd.getContactPerson());
        w.setContactPhone(cmd.getContactPhone());
        w.setLength(cmd.getLength());
        w.setWidth(cmd.getWidth());
        w.setHeight(cmd.getHeight());
        w.setStatus(1);  // 新建默认启用
        return w;
    }

    /** UpdateCmd → 领域实体（仅合并非空字段） */
    public void mergeToEntity(WarehouseUpdateCmd cmd, Warehouse warehouse) {
        warehouse.setWhCode(cmd.getWhCode());
        warehouse.setWhName(cmd.getWhName());
        warehouse.setWhType(cmd.getWhType());
        warehouse.setAddress(cmd.getAddress());
        warehouse.setContactPerson(cmd.getContactPerson());
        warehouse.setContactPhone(cmd.getContactPhone());
        warehouse.setLength(cmd.getLength());
        warehouse.setWidth(cmd.getWidth());
        warehouse.setHeight(cmd.getHeight());
    }

    /** 领域实体 → DTO */
    public WarehouseDTO toDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setWhCode(warehouse.getWhCode());
        dto.setWhName(warehouse.getWhName());
        dto.setWhType(warehouse.getWhType());
        dto.setAddress(warehouse.getAddress());
        dto.setContactPerson(warehouse.getContactPerson());
        dto.setContactPhone(warehouse.getContactPhone());
        dto.setLength(warehouse.getLength());
        dto.setWidth(warehouse.getWidth());
        dto.setHeight(warehouse.getHeight());
        dto.setStatus(warehouse.getStatus());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        return dto;
    }
}
