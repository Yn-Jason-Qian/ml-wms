package com.wms.masterdata.application.service;

import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.DictTypeAssembler;
import com.wms.masterdata.application.dto.DictTypeCreateCmd;
import com.wms.masterdata.application.dto.DictTypeDTO;
import com.wms.masterdata.domain.entity.DictType;
import com.wms.masterdata.domain.repository.DictTypeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictTypeAppService {
    private final DictTypeRepository dictTypeRepository;
    private final DictTypeAssembler assembler;

    /** 查询所有字典类型（含中文名） */
    public List<DictTypeDTO> listAll() {
        Long tenantId = UserContext.getTenantId();
        return dictTypeRepository.findAll(tenantId).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DictTypeDTO create(DictTypeCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        if (dictTypeRepository.findByCode(tenantId, cmd.getTypeCode()).isPresent()) {
            throw BusinessException.conflict("字典类型编码已存在");
        }
        DictType dictType = new DictType();
        dictType.setTypeCode(cmd.getTypeCode());
        dictType.setTypeName(cmd.getTypeName());
        dictType.setStatus(1);
        dictType.setTenantId(tenantId);
        dictType.setCreatedBy(UserContext.getUserId());
        dictType.setUpdatedBy(UserContext.getUserId());
        dictTypeRepository.save(dictType);
        return assembler.toDTO(dictType);
    }
}
