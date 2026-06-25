package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.OrderHeader;
import com.wms.outbound.domain.entity.OrderLine;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<OrderHeader> findById(Long id);

    void save(OrderHeader h);

    void saveLine(OrderLine l);

    void update(OrderHeader h);

    void updateLine(OrderLine l);

    List<OrderLine> findLines(Long headerId);

    List<OrderHeader> findByStatus(Long tenantId, Long warehouseId, String status);
}
