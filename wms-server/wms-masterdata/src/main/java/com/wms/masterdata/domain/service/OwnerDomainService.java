package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Owner;
import com.wms.masterdata.domain.repository.OwnerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerDomainService {
    private final OwnerRepository ownerRepository;

    public void validateCreate(Owner owner) {
        owner.validateCode();
        if (ownerRepository.existsByCode(owner.getTenantId(), owner.getOwnerCode(), null)) {
            throw BusinessException.conflict("货主编码 [" + owner.getOwnerCode() + "] 已存在");
        }
    }

    public void validateUpdate(Owner owner) {
        owner.validateCode();
        if (ownerRepository.existsByCode(
                owner.getTenantId(), owner.getOwnerCode(), owner.getId())) {
            throw BusinessException.conflict("货主编码 [" + owner.getOwnerCode() + "] 已存在");
        }
    }

    public void disableOwner(Owner owner) {
        if (!owner.isEnabled()) throw new BusinessException("货主已处于禁用状态");
        owner.disable();
    }

    public void enableOwner(Owner owner) {
        if (owner.isEnabled()) throw new BusinessException("货主已处于启用状态");
        owner.enable();
    }
}
