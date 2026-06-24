package com.wms.masterdata.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.OwnerAssembler;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Owner;
import com.wms.masterdata.domain.repository.OwnerRepository;
import com.wms.masterdata.domain.service.OwnerDomainService;
import com.wms.masterdata.infrastructure.mapper.OwnerMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerAppService {
    private final OwnerRepository ownerRepository;
    private final OwnerMapper ownerMapper;
    private final OwnerDomainService domainService;
    private final OwnerAssembler assembler;

    public OwnerDTO findById(Long id) {
        return assembler.toDTO(
                ownerRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("货主不存在")));
    }

    public List<OwnerDTO> findAll() {
        return ownerRepository.findAll(UserContext.getTenantId()).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    public IPage<OwnerDTO> page(OwnerPageQuery query) {
        Page<Owner> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Owner> result =
                ownerMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<Owner>()
                                .eq(Owner::getTenantId, UserContext.getTenantId())
                                .eq(
                                        query.getOwnerCode() != null,
                                        Owner::getOwnerCode,
                                        query.getOwnerCode())
                                .like(
                                        query.getOwnerName() != null,
                                        Owner::getOwnerName,
                                        query.getOwnerName()));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public OwnerDTO create(OwnerCreateCmd cmd) {
        Owner owner = assembler.toEntity(cmd);
        owner.setTenantId(UserContext.getTenantId());
        owner.setCreatedBy(UserContext.getUserId());
        owner.setUpdatedBy(UserContext.getUserId());
        domainService.validateCreate(owner);
        ownerRepository.save(owner);
        return assembler.toDTO(owner);
    }

    @Transactional
    public OwnerDTO update(OwnerUpdateCmd cmd) {
        Owner owner =
                ownerRepository
                        .findById(cmd.getId())
                        .orElseThrow(() -> BusinessException.notFound("货主不存在"));
        assembler.mergeToEntity(cmd, owner);
        owner.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(owner);
        ownerRepository.update(owner);
        return assembler.toDTO(owner);
    }

    @Transactional
    public void delete(Long id) {
        ownerRepository.deleteById(id);
    }

    @Transactional
    public void disable(Long id) {
        Owner owner =
                ownerRepository.findById(id).orElseThrow(() -> BusinessException.notFound("货主不存在"));
        domainService.disableOwner(owner);
        ownerRepository.update(owner);
    }

    @Transactional
    public void enable(Long id) {
        Owner owner =
                ownerRepository.findById(id).orElseThrow(() -> BusinessException.notFound("货主不存在"));
        domainService.enableOwner(owner);
        ownerRepository.update(owner);
    }
}
