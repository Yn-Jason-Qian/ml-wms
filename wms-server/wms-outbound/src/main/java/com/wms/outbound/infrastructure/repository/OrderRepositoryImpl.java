package com.wms.outbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.outbound.domain.entity.OrderHeader;
import com.wms.outbound.domain.entity.OrderLine;
import com.wms.outbound.domain.repository.OrderRepository;
import com.wms.outbound.infrastructure.mapper.OrderHeaderMapper;
import com.wms.outbound.infrastructure.mapper.OrderLineMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderHeaderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    @Override
    public void save(OrderHeader h) {
        orderMapper.insert(h);
    }

    @Override
    public void saveLine(OrderLine l) {
        orderLineMapper.insert(l);
    }

    @Override
    public void update(OrderHeader h) {
        orderMapper.updateById(h);
    }

    @Override
    public void updateLine(OrderLine l) {
        orderLineMapper.updateById(l);
    }

    @Override
    public List<OrderLine> findLines(Long hId) {
        return orderLineMapper.selectList(
                new LambdaQueryWrapper<OrderLine>()
                        .eq(OrderLine::getOrderHeaderId, hId)
                        .orderByAsc(OrderLine::getLineNo));
    }

    @Override
    public List<OrderHeader> findByStatus(Long tId, Long wId, String s) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<OrderHeader>()
                        .eq(OrderHeader::getTenantId, tId)
                        .eq(OrderHeader::getWarehouseId, wId)
                        .eq(OrderHeader::getStatus, s));
    }
}
