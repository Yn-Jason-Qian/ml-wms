package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.DictionaryCreateCmd;
import com.wms.masterdata.application.dto.DictionaryDTO;
import com.wms.masterdata.application.dto.DictionaryUpdateCmd;
import com.wms.masterdata.domain.entity.Dictionary;
import org.springframework.stereotype.Component;

@Component
public class DictionaryAssembler {
    public Dictionary toEntity(DictionaryCreateCmd cmd) {
        Dictionary d = new Dictionary();
        d.setDictType(cmd.getDictType());
        d.setDictCode(cmd.getDictCode());
        d.setDictName(cmd.getDictName());
        d.setParentCode(cmd.getParentCode() != null ? cmd.getParentCode() : "0");
        d.setSortOrder(cmd.getSortOrder() != null ? cmd.getSortOrder() : 0);
        d.setExtra(cmd.getExtra());
        d.setStatus(1);
        return d;
    }

    public void mergeToEntity(DictionaryUpdateCmd cmd, Dictionary d) {
        d.setDictCode(cmd.getDictCode());
        d.setDictName(cmd.getDictName());
        d.setParentCode(cmd.getParentCode() != null ? cmd.getParentCode() : "0");
        d.setSortOrder(cmd.getSortOrder() != null ? cmd.getSortOrder() : 0);
        d.setExtra(cmd.getExtra());
    }

    public DictionaryDTO toDTO(Dictionary d) {
        DictionaryDTO dto = new DictionaryDTO();
        dto.setId(d.getId());
        dto.setDictType(d.getDictType());
        dto.setDictCode(d.getDictCode());
        dto.setDictName(d.getDictName());
        dto.setParentCode(d.getParentCode());
        dto.setSortOrder(d.getSortOrder());
        dto.setExtra(d.getExtra());
        dto.setStatus(d.getStatus());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }
}
