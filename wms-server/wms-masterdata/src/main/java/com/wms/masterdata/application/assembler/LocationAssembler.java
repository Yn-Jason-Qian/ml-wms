package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.LocationCreateCmd;
import com.wms.masterdata.application.dto.LocationDTO;
import com.wms.masterdata.application.dto.LocationUpdateCmd;
import com.wms.masterdata.domain.entity.Location;

import org.springframework.stereotype.Component;

@Component
public class LocationAssembler {
    public Location toEntity(LocationCreateCmd cmd) {
        Location l = new Location();
        l.setWarehouseId(cmd.getWarehouseId());
        l.setAreaId(cmd.getAreaId());
        l.setLocationCode(cmd.getLocationCode());
        l.setLocationName(cmd.getLocationName());
        l.setLocationType(cmd.getLocationType());
        l.setAisle(cmd.getAisle());
        l.setShelf(cmd.getShelf());
        l.setTier(cmd.getTier());
        l.setDepthPos(cmd.getDepthPos());
        l.setMaxWeight(cmd.getMaxWeight());
        l.setMaxVolume(cmd.getMaxVolume());
        l.setMaxQty(cmd.getMaxQty());
        l.setLength(cmd.getLength());
        l.setWidth(cmd.getWidth());
        l.setHeight(cmd.getHeight());
        l.setRoadway(cmd.getRoadway());
        l.setStatus(Location.STATUS_IDLE);
        return l;
    }

    public void mergeToEntity(LocationUpdateCmd cmd, Location l) {
        l.setLocationCode(cmd.getLocationCode());
        l.setLocationName(cmd.getLocationName());
        l.setLocationType(cmd.getLocationType());
        l.setAisle(cmd.getAisle());
        l.setShelf(cmd.getShelf());
        l.setTier(cmd.getTier());
        l.setDepthPos(cmd.getDepthPos());
        l.setMaxWeight(cmd.getMaxWeight());
        l.setMaxVolume(cmd.getMaxVolume());
        l.setMaxQty(cmd.getMaxQty());
        l.setLength(cmd.getLength());
        l.setWidth(cmd.getWidth());
        l.setHeight(cmd.getHeight());
        l.setRoadway(cmd.getRoadway());
    }

    public LocationDTO toDTO(Location l) {
        LocationDTO dto = new LocationDTO();
        dto.setId(l.getId());
        dto.setWarehouseId(l.getWarehouseId());
        dto.setAreaId(l.getAreaId());
        dto.setLocationCode(l.getLocationCode());
        dto.setLocationName(l.getLocationName());
        dto.setLocationType(l.getLocationType());
        dto.setAisle(l.getAisle());
        dto.setShelf(l.getShelf());
        dto.setTier(l.getTier());
        dto.setDepthPos(l.getDepthPos());
        dto.setMaxWeight(l.getMaxWeight());
        dto.setMaxVolume(l.getMaxVolume());
        dto.setMaxQty(l.getMaxQty());
        dto.setLength(l.getLength());
        dto.setWidth(l.getWidth());
        dto.setHeight(l.getHeight());
        dto.setRoadway(l.getRoadway());
        dto.setStatus(l.getStatus());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        return dto;
    }
}
