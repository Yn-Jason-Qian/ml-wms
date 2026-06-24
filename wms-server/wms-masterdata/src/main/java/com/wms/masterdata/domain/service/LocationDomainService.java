package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Location;
import com.wms.masterdata.domain.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationDomainService {
    private final LocationRepository locationRepository;

    public void validateCreate(Location location) {
        if (locationRepository.existsByCode(
                location.getTenantId(),
                location.getWarehouseId(),
                location.getLocationCode(),
                null)) {
            throw BusinessException.conflict("库位编码 [" + location.getLocationCode() + "] 已存在");
        }
    }

    public void validateUpdate(Location location) {
        if (locationRepository.existsByCode(
                location.getTenantId(),
                location.getWarehouseId(),
                location.getLocationCode(),
                location.getId())) {
            throw BusinessException.conflict("库位编码 [" + location.getLocationCode() + "] 已存在");
        }
    }

    public void disableLocation(Location location) {
        if (!location.isEnabled()) throw new BusinessException("库位已处于禁用状态");
        location.disable();
    }

    public void enableLocation(Location location) {
        if (location.isEnabled()) throw new BusinessException("库位已处于启用状态");
        location.enable();
    }
}
