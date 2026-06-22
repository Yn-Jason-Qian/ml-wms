package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Dictionary;
import com.wms.masterdata.domain.repository.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictionaryDomainService {
    private final DictionaryRepository dictionaryRepository;

    public void validateCreate(Dictionary dict) {
        if (dictionaryRepository.existsByCode(dict.getTenantId(), dict.getDictType(), dict.getDictCode(), null)) {
            throw BusinessException.conflict("字典编码 [" + dict.getDictCode() + "] 已存在");
        }
    }

    public void validateUpdate(Dictionary dict) {
        if (dictionaryRepository.existsByCode(dict.getTenantId(), dict.getDictType(), dict.getDictCode(), dict.getId())) {
            throw BusinessException.conflict("字典编码 [" + dict.getDictCode() + "] 已存在");
        }
    }
}
