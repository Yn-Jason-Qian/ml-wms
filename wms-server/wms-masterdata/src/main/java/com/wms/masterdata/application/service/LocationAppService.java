package com.wms.masterdata.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.LocationAssembler;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Location;
import com.wms.masterdata.domain.repository.LocationRepository;
import com.wms.masterdata.domain.service.LocationDomainService;
import com.wms.masterdata.infrastructure.mapper.LocationMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationAppService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final LocationDomainService domainService;
    private final LocationAssembler assembler;

    public LocationDTO findById(Long id) {
        Location l =
                locationRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("库位不存在"));
        return assembler.toDTO(l);
    }

    public List<LocationDTO> findByArea(Long areaId) {
        return locationRepository.findByArea(UserContext.getTenantId(), areaId).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    // ───── 跨域调用（供其他模块 Gateway Adapter 使用）─────

    /**
     * 根据 ID 或 code 查找库位（支持 PDA 扫码传 code 的场景）。
     *
     * @param locationId 库位 ID，可为 null
     * @param locationCode 库位编码，可为 null
     * @param warehouseId 仓库 ID，按编码查找时用于限定范围，可为 null
     * @param tenantId 租户 ID
     * @return 库位实体
     */
    public Location resolveLocation(
            Long locationId, String locationCode, Long warehouseId, Long tenantId) {
        if (locationId != null) {
            return locationRepository
                    .findById(locationId)
                    .orElseThrow(() -> BusinessException.notFound("库位不存在: id=" + locationId));
        }
        if (locationCode != null && !locationCode.isBlank()) {
            return locationRepository
                    .findByCode(tenantId, warehouseId, locationCode)
                    .orElseThrow(() -> BusinessException.notFound("库位不存在: code=" + locationCode));
        }
        throw BusinessException.badRequest("locationId 或 locationCode 必须提供一个");
    }

    /** 批量查询库位编码，供其他域在列表场景补全展示字段。 */
    public Map<Long, String> findCodeMap(Collection<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = locationIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return locationMapper
                .selectList(new LambdaQueryWrapper<Location>().in(Location::getId, ids))
                .stream()
                .collect(Collectors.toMap(Location::getId, Location::getLocationCode, (a, b) -> a));
    }

    public IPage<LocationDTO> page(LocationPageQuery query) {
        Page<Location> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Location> result =
                locationMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<Location>()
                                .eq(Location::getTenantId, UserContext.getTenantId())
                                .eq(
                                        query.getWarehouseId() != null,
                                        Location::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getAreaId() != null,
                                        Location::getAreaId,
                                        query.getAreaId())
                                .eq(
                                        query.getLocationCode() != null,
                                        Location::getLocationCode,
                                        query.getLocationCode())
                                .eq(
                                        query.getLocationType() != null,
                                        Location::getLocationType,
                                        query.getLocationType())
                                .eq(
                                        query.getStatus() != null,
                                        Location::getStatus,
                                        query.getStatus()));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public LocationDTO create(LocationCreateCmd cmd) {
        Location l = assembler.toEntity(cmd);
        l.setTenantId(UserContext.getTenantId());
        l.setCreatedBy(UserContext.getUserId());
        l.setUpdatedBy(UserContext.getUserId());
        domainService.validateCreate(l);
        locationRepository.save(l);
        return assembler.toDTO(l);
    }

    @Transactional
    public List<LocationDTO> batchCreate(LocationBatchCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        List<Location> locations = new ArrayList<>();

        for (int a = cmd.getAisleFrom(); a <= cmd.getAisleTo(); a++) {
            for (int s = cmd.getShelfFrom(); s <= cmd.getShelfTo(); s++) {
                for (int t = cmd.getTierFrom(); t <= cmd.getTierTo(); t++) {
                    for (int d = cmd.getDepthFrom(); d <= cmd.getDepthTo(); d++) {
                        Location l = new Location();
                        l.setWarehouseId(cmd.getWarehouseId());
                        l.setAreaId(cmd.getAreaId());
                        l.setLocationCode(
                                Location.generateCode(
                                        cmd.getWarehousePrefix(),
                                        String.format("%02d", a),
                                        String.format("%02d", s),
                                        String.format("%02d", t),
                                        String.format("%02d", d)));
                        l.setLocationType(cmd.getLocationType());
                        l.setAisle(String.format("%02d", a));
                        l.setShelf(String.format("%02d", s));
                        l.setTier(String.format("%02d", t));
                        l.setDepthPos(String.format("%02d", d));
                        l.setMaxWeight(cmd.getMaxWeight());
                        l.setMaxVolume(cmd.getMaxVolume());
                        l.setMaxQty(cmd.getMaxQty());
                        l.setStatus(Location.STATUS_IDLE);
                        l.setTenantId(tenantId);
                        l.setCreatedBy(userId);
                        l.setUpdatedBy(userId);
                        locations.add(l);
                    }
                }
            }
        }
        locationRepository.batchSave(locations);
        return locations.stream().map(assembler::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public LocationDTO update(LocationUpdateCmd cmd) {
        Location l =
                locationRepository
                        .findById(cmd.getId())
                        .orElseThrow(() -> BusinessException.notFound("库位不存在"));
        assembler.mergeToEntity(cmd, l);
        l.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(l);
        locationRepository.update(l);
        return assembler.toDTO(l);
    }

    @Transactional
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    @Transactional
    public void disable(Long id) {
        Location l =
                locationRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("库位不存在"));
        domainService.disableLocation(l);
        locationRepository.update(l);
    }

    @Transactional
    public void enable(Long id) {
        Location l =
                locationRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("库位不存在"));
        domainService.enableLocation(l);
        locationRepository.update(l);
    }
}
