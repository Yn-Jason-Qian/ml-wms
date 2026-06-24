package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Area;
import com.wms.masterdata.domain.repository.AreaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AreaDomainService {
    private final AreaRepository areaRepository;

    public void validateCreate(Area area) {
        area.validateCode();
        if (areaRepository.existsByCode(
                area.getTenantId(), area.getWarehouseId(), area.getAreaCode(), null)) {
            throw BusinessException.conflict("库区编码 [" + area.getAreaCode() + "] 已存在");
        }
    }

    public void validateUpdate(Area area) {
        area.validateCode();
        if (areaRepository.existsByCode(
                area.getTenantId(), area.getWarehouseId(), area.getAreaCode(), area.getId())) {
            throw BusinessException.conflict("库区编码 [" + area.getAreaCode() + "] 已存在");
        }
    }

    public void disableArea(Area area) {
        if (!area.isEnabled()) throw new BusinessException("库区已处于禁用状态");
        area.disable();
    }

    public void enableArea(Area area) {
        if (area.isEnabled()) throw new BusinessException("库区已处于启用状态");
        area.enable();
    }
}
