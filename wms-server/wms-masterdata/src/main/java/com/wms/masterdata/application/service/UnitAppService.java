package com.wms.masterdata.application.service;

import com.wms.common.context.UserContext;
import com.wms.masterdata.application.assembler.UnitAssembler;
import com.wms.masterdata.application.dto.UnitDTO;
import com.wms.masterdata.domain.repository.UnitRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitAppService {
    private final UnitRepository unitRepository;
    private final UnitAssembler assembler;

    public List<UnitDTO> findAll() {
        return unitRepository.findAll(UserContext.getTenantId()).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }
}
