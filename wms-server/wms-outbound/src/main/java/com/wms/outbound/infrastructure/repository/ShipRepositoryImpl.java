package com.wms.outbound.infrastructure.repository;

import com.wms.outbound.domain.entity.ShipHeader;
import com.wms.outbound.domain.repository.ShipRepository;
import com.wms.outbound.infrastructure.mapper.ShipHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipRepositoryImpl implements ShipRepository {
    private final ShipHeaderMapper shipMapper;

    @Override
    public void saveHeader(ShipHeader h) {
        shipMapper.insert(h);
    }

    @Override
    public Optional<ShipHeader> findById(Long id) {
        return Optional.ofNullable(shipMapper.selectById(id));
    }
}
