package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.ShipHeader;

import java.util.Optional;

public interface ShipRepository {
    void saveHeader(ShipHeader h);

    Optional<ShipHeader> findById(Long id);
}
