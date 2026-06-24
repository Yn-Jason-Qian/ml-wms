package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.OwnerCreateCmd;
import com.wms.masterdata.application.dto.OwnerDTO;
import com.wms.masterdata.application.dto.OwnerUpdateCmd;
import com.wms.masterdata.domain.entity.Owner;

import org.springframework.stereotype.Component;

@Component
public class OwnerAssembler {
    public Owner toEntity(OwnerCreateCmd cmd) {
        Owner o = new Owner();
        o.setOwnerCode(cmd.getOwnerCode());
        o.setOwnerName(cmd.getOwnerName());
        o.setContactPerson(cmd.getContactPerson());
        o.setContactPhone(cmd.getContactPhone());
        o.setAddress(cmd.getAddress());
        o.setStatus(1);
        return o;
    }

    public void mergeToEntity(OwnerUpdateCmd cmd, Owner o) {
        o.setOwnerCode(cmd.getOwnerCode());
        o.setOwnerName(cmd.getOwnerName());
        o.setContactPerson(cmd.getContactPerson());
        o.setContactPhone(cmd.getContactPhone());
        o.setAddress(cmd.getAddress());
    }

    public OwnerDTO toDTO(Owner o) {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(o.getId());
        dto.setOwnerCode(o.getOwnerCode());
        dto.setOwnerName(o.getOwnerName());
        dto.setContactPerson(o.getContactPerson());
        dto.setContactPhone(o.getContactPhone());
        dto.setAddress(o.getAddress());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        return dto;
    }
}
