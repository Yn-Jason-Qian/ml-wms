package com.wms.outbound.domain.repository;

import com.wms.outbound.domain.entity.*;

import java.util.List;
import java.util.Optional;

public interface OutboundRepository {
    // Order
    Optional<OrderHeader> findOrderById(Long id);

    void saveOrder(OrderHeader h);

    void saveOrderLine(OrderLine l);

    void updateOrderLine(OrderLine l);

    List<OrderLine> findOrderLines(Long headerId);

    void updateOrder(OrderHeader h);

    List<OrderHeader> findOrdersByStatus(Long tenantId, Long warehouseId, String status);

    // Wave
    Optional<WaveHeader> findWaveById(Long id);

    void saveWave(WaveHeader h);

    void updateWave(WaveHeader h);

    void saveWaveLine(WaveLine l);

    List<WaveLine> findWaveLines(Long waveId);

    // Pick
    Optional<PickHeader> findPickById(Long id);

    Optional<PickHeader> findPickByWave(Long waveId);

    void savePickHeader(PickHeader h);

    void savePickLine(PickLine l);

    void updatePickLine(PickLine l);

    void updatePickHeader(PickHeader h);

    List<PickLine> findPickLines(Long headerId);

    // Check
    void saveCheckHeader(CheckHeader h);

    void saveCheckLine(CheckLine l);

    void updateCheckHeader(CheckHeader h);

    Optional<CheckHeader> findCheckById(Long id);

    // Ship
    void saveShipHeader(ShipHeader h);

    void saveShipLine(ShipLine l);

    Optional<ShipHeader> findShipById(Long id);
}
