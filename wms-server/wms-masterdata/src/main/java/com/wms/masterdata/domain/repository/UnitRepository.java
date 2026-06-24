package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Unit;

import java.util.List;

public interface UnitRepository {
    List<Unit> findAll(Long tenantId);
}
