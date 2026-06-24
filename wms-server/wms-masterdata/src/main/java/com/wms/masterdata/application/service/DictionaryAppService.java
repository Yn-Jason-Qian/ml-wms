package com.wms.masterdata.application.service;

import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.DictionaryAssembler;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Dictionary;
import com.wms.masterdata.domain.repository.DictionaryRepository;
import com.wms.masterdata.domain.service.DictionaryDomainService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictionaryAppService {
    private final DictionaryRepository dictRepository;
    private final DictionaryDomainService domainService;
    private final DictionaryAssembler assembler;

    /** 按类型查询字典项列表 */
    public List<DictionaryDTO> listByType(String dictType) {
        return dictRepository.findByType(UserContext.getTenantId(), dictType).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    /** 查询所有字典类型 */
    public List<String> listTypes() {
        return dictRepository.findAllTypes(UserContext.getTenantId());
    }

    @Transactional
    public DictionaryDTO create(DictionaryCreateCmd cmd) {
        Dictionary dict = assembler.toEntity(cmd);
        dict.setTenantId(UserContext.getTenantId());
        dict.setCreatedBy(UserContext.getUserId());
        dict.setUpdatedBy(UserContext.getUserId());
        domainService.validateCreate(dict);
        dictRepository.save(dict);
        return assembler.toDTO(dict);
    }

    @Transactional
    public DictionaryDTO update(DictionaryUpdateCmd cmd) {
        Dictionary dict =
                dictRepository
                        .findById(cmd.getId())
                        .orElseThrow(() -> BusinessException.notFound("字典项不存在"));
        assembler.mergeToEntity(cmd, dict);
        dict.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(dict);
        dictRepository.update(dict);
        return assembler.toDTO(dict);
    }

    @Transactional
    public void enable(Long id) {
        Dictionary dict =
                dictRepository.findById(id).orElseThrow(() -> BusinessException.notFound("字典项不存在"));
        dict.enable();
        dict.setUpdatedBy(UserContext.getUserId());
        dictRepository.update(dict);
    }

    @Transactional
    public void disable(Long id) {
        Dictionary dict =
                dictRepository.findById(id).orElseThrow(() -> BusinessException.notFound("字典项不存在"));
        dict.disable();
        dict.setUpdatedBy(UserContext.getUserId());
        dictRepository.update(dict);
    }

    @Transactional
    public void delete(Long id) {
        dictRepository.deleteById(id);
    }
}
