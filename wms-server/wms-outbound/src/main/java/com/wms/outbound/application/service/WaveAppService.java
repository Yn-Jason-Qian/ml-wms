package com.wms.outbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.outbound.application.assembler.OrderAssembler;
import com.wms.outbound.application.assembler.WaveAssembler;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.OrderRepository;
import com.wms.outbound.domain.repository.WaveRepository;
import com.wms.outbound.infrastructure.mapper.OrderHeaderMapper;
import com.wms.outbound.infrastructure.mapper.WaveHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaveAppService {
    private final WaveRepository waveRepository;
    private final WaveHeaderMapper waveMapper;
    private final OrderRepository orderRepository;
    private final OrderHeaderMapper orderMapper;
    private final WaveAssembler waveAssembler;
    private final OrderAssembler orderAssembler;

    public IPage<WaveDTO> pageWaves(WavePageQuery query) {
        IPage<WaveHeader> result =
                waveMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<WaveHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        WaveHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getWaveStatus() != null,
                                        WaveHeader::getWaveStatus,
                                        query.getWaveStatus())
                                .orderByDesc(WaveHeader::getCreatedAt));
        return result.convert(waveAssembler::toDTO);
    }

    public WaveDTO getWave(Long id) {
        WaveHeader h =
                waveRepository.findById(id).orElseThrow(() -> BusinessException.notFound("波次不存在"));
        WaveDTO dto = waveAssembler.toDTO(h);
        List<WaveOrderDTO> orders =
                waveRepository.findLines(id).stream()
                        .map(wl -> orderMapper.selectById(wl.getOrderHeaderId()))
                        .filter(o -> o != null)
                        .map(orderAssembler::toOrderRefDTO)
                        .toList();
        dto.setOrders(orders);
        return dto;
    }

    @Transactional
    public WaveResultDTO createWave(WaveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String waveNo =
                "WAV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        WaveHeader h = new WaveHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setWaveNo(waveNo);
        h.setWaveType(cmd.getWaveType());
        h.setWaveStatus(WaveHeader.STATUS_CREATED);
        h.setOrderCount(cmd.getOrderIds().size());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        waveRepository.save(h);

        int sort = 0;
        for (Long orderId : cmd.getOrderIds()) {
            WaveLine wl = new WaveLine();
            wl.setTenantId(tenantId);
            wl.setWaveHeaderId(h.getId());
            wl.setOrderHeaderId(orderId);
            wl.setSortOrder(++sort);
            wl.setCreatedBy(userId);
            wl.setUpdatedBy(userId);
            waveRepository.saveLine(wl);

            OrderHeader order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setWaveHeaderId(h.getId());
                orderRepository.update(order);
            }
        }

        h.release();
        h.setReleasedBy(userId);
        waveRepository.update(h);
        return new WaveResultDTO(waveNo, h.getId());
    }
}
