package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.DictTypeDTO;
import com.wms.masterdata.domain.entity.DictType;
import org.springframework.stereotype.Component;

@Component
public class DictTypeAssembler {

    public DictTypeDTO toDTO(DictType entity) {
        DictTypeDTO dto = new DictTypeDTO();
        dto.setTypeCode(entity.getTypeCode());
        dto.setTypeName(entity.getTypeName());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
