package com.wms.outbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.OutboundRepository;
import com.wms.outbound.infrastructure.mapper.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OutboundRepositoryImpl implements OutboundRepository {
    private final OrderHeaderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final WaveHeaderMapper waveMapper;
    private final WaveLineMapper waveLineMapper;
    private final PickHeaderMapper pickMapper;
    private final PickLineMapper pickLineMapper;
    private final CheckHeaderMapper checkMapper;
    private final CheckLineMapper checkLineMapper;
    private final ShipHeaderMapper shipMapper;
    private final ShipLineMapper shipLineMapper;

    @Override
    public Optional<OrderHeader> findOrderById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    @Override
    public void saveOrder(OrderHeader h) {
        orderMapper.insert(h);
    }

    @Override
    public void saveOrderLine(OrderLine l) {
        orderLineMapper.insert(l);
    }

    @Override
    public void updateOrder(OrderHeader h) {
        orderMapper.updateById(h);
    }

    @Override
    public List<OrderLine> findOrderLines(Long hId) {
        return orderLineMapper.selectList(
                new LambdaQueryWrapper<OrderLine>()
                        .eq(OrderLine::getOrderHeaderId, hId)
                        .orderByAsc(OrderLine::getLineNo));
    }

    @Override
    public List<OrderHeader> findOrdersByStatus(Long tId, Long wId, String s) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<OrderHeader>()
                        .eq(OrderHeader::getTenantId, tId)
                        .eq(OrderHeader::getWarehouseId, wId)
                        .eq(OrderHeader::getStatus, s));
    }

    @Override
    public Optional<WaveHeader> findWaveById(Long id) {
        return Optional.ofNullable(waveMapper.selectById(id));
    }

    @Override
    public void saveWave(WaveHeader h) {
        waveMapper.insert(h);
    }

    @Override
    public void updateWave(WaveHeader h) {
        waveMapper.updateById(h);
    }

    @Override
    public void saveWaveLine(WaveLine l) {
        waveLineMapper.insert(l);
    }

    @Override
    public List<WaveLine> findWaveLines(Long wId) {
        return waveLineMapper.selectList(
                new LambdaQueryWrapper<WaveLine>().eq(WaveLine::getWaveHeaderId, wId));
    }

    @Override
    public Optional<PickHeader> findPickById(Long id) {
        return Optional.ofNullable(pickMapper.selectById(id));
    }

    @Override
    public Optional<PickHeader> findPickByWave(Long waveId) {
        return Optional.ofNullable(
                pickMapper.selectOne(
                        new LambdaQueryWrapper<PickHeader>()
                                .eq(PickHeader::getWaveHeaderId, waveId)
                                .orderByDesc(PickHeader::getCreatedAt)
                                .last("LIMIT 1")));
    }

    @Override
    public void savePickHeader(PickHeader h) {
        pickMapper.insert(h);
    }

    @Override
    public void savePickLine(PickLine l) {
        pickLineMapper.insert(l);
    }

    @Override
    public void updatePickLine(PickLine l) {
        pickLineMapper.updateById(l);
    }

    @Override
    public void updatePickHeader(PickHeader h) {
        pickMapper.updateById(h);
    }

    @Override
    public List<PickLine> findPickLines(Long hId) {
        return pickLineMapper.selectList(
                new LambdaQueryWrapper<PickLine>().eq(PickLine::getPickHeaderId, hId));
    }

    @Override
    public void saveCheckHeader(CheckHeader h) {
        checkMapper.insert(h);
    }

    @Override
    public void saveCheckLine(CheckLine l) {
        checkLineMapper.insert(l);
    }

    @Override
    public void updateCheckHeader(CheckHeader h) {
        checkMapper.updateById(h);
    }

    @Override
    public Optional<CheckHeader> findCheckById(Long id) {
        return Optional.ofNullable(checkMapper.selectById(id));
    }

    @Override
    public void saveShipHeader(ShipHeader h) {
        shipMapper.insert(h);
    }

    @Override
    public void saveShipLine(ShipLine l) {
        shipLineMapper.insert(l);
    }

    @Override
    public Optional<ShipHeader> findShipById(Long id) {
        return Optional.ofNullable(shipMapper.selectById(id));
    }
}
