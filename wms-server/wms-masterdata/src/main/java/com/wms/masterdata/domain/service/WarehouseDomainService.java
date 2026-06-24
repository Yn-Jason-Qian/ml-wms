package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Warehouse;
import com.wms.masterdata.domain.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/** 仓库领域服务 —— 封装跨聚合根的领域逻辑、业务规则校验。 不标注 @Transactional（事务由应用层管理）。 */
@Service
@RequiredArgsConstructor
public class WarehouseDomainService {

    private final WarehouseRepository warehouseRepository;

    /** 创建仓库 —— 领域规则校验 */
    public void validateCreate(Warehouse warehouse) {
        warehouse.validateCode();
        if (warehouseRepository.existsByCode(
                warehouse.getTenantId(), warehouse.getWhCode(), null)) {
            throw BusinessException.conflict("仓库编码 [" + warehouse.getWhCode() + "] 已存在");
        }
    }

    /** 更新仓库 —— 校验编码唯一性（排除自身） */
    public void validateUpdate(Warehouse warehouse) {
        warehouse.validateCode();
        if (warehouseRepository.existsByCode(
                warehouse.getTenantId(), warehouse.getWhCode(), warehouse.getId())) {
            throw BusinessException.conflict("仓库编码 [" + warehouse.getWhCode() + "] 已存在");
        }
    }

    /** 禁用仓库 —— 业务规则：确认无库存后可禁用 此处仅做规则校验，具体库存检查由 Inventory 域通过事件实现。 */
    public void disableWarehouse(Warehouse warehouse) {
        if (!warehouse.isEnabled()) {
            throw new BusinessException("仓库已处于禁用状态");
        }
        warehouse.disable();
    }

    /** 启用仓库 */
    public void enableWarehouse(Warehouse warehouse) {
        if (warehouse.isEnabled()) {
            throw new BusinessException("仓库已处于启用状态");
        }
        warehouse.enable();
    }
}
