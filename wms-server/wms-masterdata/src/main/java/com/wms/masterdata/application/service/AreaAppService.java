package com.wms.masterdata.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.AreaAssembler;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Area;
import com.wms.masterdata.domain.repository.AreaRepository;
import com.wms.masterdata.domain.service.AreaDomainService;
import com.wms.masterdata.infrastructure.mapper.AreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaAppService {
    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;
    private final AreaDomainService domainService;
    private final AreaAssembler assembler;

    public AreaDTO findById(Long id) {
        Area a = areaRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库区不存在"));
        return assembler.toDTO(a);
    }

    public List<AreaDTO> findByWarehouse(Long warehouseId) {
        return areaRepository.findByWarehouse(UserContext.getTenantId(), warehouseId)
                .stream().map(assembler::toDTO).collect(Collectors.toList());
    }

    public IPage<AreaDTO> page(AreaPageQuery query) {
        Page<Area> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Area> result = areaMapper.selectPage(page, new LambdaQueryWrapper<Area>()
                .eq(Area::getTenantId, UserContext.getTenantId())
                .eq(query.getWarehouseId() != null, Area::getWarehouseId, query.getWarehouseId())
                .eq(query.getAreaCode() != null, Area::getAreaCode, query.getAreaCode())
                .like(query.getAreaName() != null, Area::getAreaName, query.getAreaName())
                .eq(query.getAreaType() != null, Area::getAreaType, query.getAreaType()));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public AreaDTO create(AreaCreateCmd cmd) {
        Area area = assembler.toEntity(cmd);
        area.setTenantId(UserContext.getTenantId());
        area.setCreatedBy(UserContext.getUserId());
        area.setUpdatedBy(UserContext.getUserId());
        domainService.validateCreate(area);
        areaRepository.save(area);
        return assembler.toDTO(area);
    }

    @Transactional
    public AreaDTO update(AreaUpdateCmd cmd) {
        Area area = areaRepository.findById(cmd.getId()).orElseThrow(() -> BusinessException.notFound("库区不存在"));
        assembler.mergeToEntity(cmd, area);
        area.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(area);
        areaRepository.update(area);
        return assembler.toDTO(area);
    }

    @Transactional public void delete(Long id) { areaRepository.deleteById(id); }

    @Transactional
    public void disable(Long id) {
        Area area = areaRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库区不存在"));
        domainService.disableArea(area);
        areaRepository.update(area);
    }

    @Transactional
    public void enable(Long id) {
        Area area = areaRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库区不存在"));
        domainService.enableArea(area);
        areaRepository.update(area);
    }
}
