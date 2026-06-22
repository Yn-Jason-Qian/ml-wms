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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationAppService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final LocationDomainService domainService;
    private final LocationAssembler assembler;

    public LocationDTO findById(Long id) {
        Location l = locationRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库位不存在"));
        return assembler.toDTO(l);
    }

    public List<LocationDTO> findByArea(Long areaId) {
        return locationRepository.findByArea(UserContext.getTenantId(), areaId)
                .stream().map(assembler::toDTO).collect(Collectors.toList());
    }

    public IPage<LocationDTO> page(LocationPageQuery query) {
        Page<Location> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Location> result = locationMapper.selectPage(page, new LambdaQueryWrapper<Location>()
                .eq(Location::getTenantId, UserContext.getTenantId())
                .eq(query.getWarehouseId() != null, Location::getWarehouseId, query.getWarehouseId())
                .eq(query.getAreaId() != null, Location::getAreaId, query.getAreaId())
                .eq(query.getLocationCode() != null, Location::getLocationCode, query.getLocationCode())
                .eq(query.getLocationType() != null, Location::getLocationType, query.getLocationType())
                .eq(query.getStatus() != null, Location::getStatus, query.getStatus()));
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
                        l.setLocationCode(Location.generateCode(cmd.getWarehousePrefix(),
                                String.format("%02d", a), String.format("%02d", s),
                                String.format("%02d", t), String.format("%02d", d)));
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
        Location l = locationRepository.findById(cmd.getId()).orElseThrow(() -> BusinessException.notFound("库位不存在"));
        assembler.mergeToEntity(cmd, l);
        l.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(l);
        locationRepository.update(l);
        return assembler.toDTO(l);
    }

    @Transactional public void delete(Long id) { locationRepository.deleteById(id); }

    @Transactional
    public void disable(Long id) {
        Location l = locationRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库位不存在"));
        domainService.disableLocation(l);
        locationRepository.update(l);
    }

    @Transactional
    public void enable(Long id) {
        Location l = locationRepository.findById(id).orElseThrow(() -> BusinessException.notFound("库位不存在"));
        domainService.enableLocation(l);
        locationRepository.update(l);
    }
}
