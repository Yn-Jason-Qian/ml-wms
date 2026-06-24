package com.wms.masterdata.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.SkuAssembler;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.masterdata.domain.entity.SkuPackage;
import com.wms.masterdata.domain.repository.SkuPackageRepository;
import com.wms.masterdata.domain.repository.SkuRepository;
import com.wms.masterdata.domain.service.SkuDomainService;
import com.wms.masterdata.infrastructure.mapper.SkuMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuAppService {
    private final SkuRepository skuRepository;
    private final SkuMapper skuMapper;
    private final SkuPackageRepository packageRepository;
    private final SkuDomainService domainService;
    private final SkuAssembler assembler;

    public SkuDTO findById(Long id) {
        Sku s = skuRepository.findById(id).orElseThrow(() -> BusinessException.notFound("SKU不存在"));
        SkuDTO dto = assembler.toDTO(s);
        // 附带包装规格
        List<SkuPackage> pkgs = packageRepository.findBySkuId(UserContext.getTenantId(), id);
        dto.setPackages(pkgs.stream().map(assembler::toPackageDTO).collect(Collectors.toList()));
        return dto;
    }

    // ───── 跨域调用（供其他模块 Gateway Adapter 使用）─────

    /**
     * 根据 ID 或 code 查找 SKU（支持 PDA 扫码传 code 的场景）。
     *
     * @param skuId SKU ID，可为 null
     * @param skuCode SKU 编码，可为 null
     * @param tenantId 租户 ID
     * @return SKU 实体
     */
    public Sku resolveSku(Long skuId, String skuCode, Long tenantId) {
        if (skuId != null) {
            return skuRepository
                    .findById(skuId)
                    .orElseThrow(() -> BusinessException.notFound("SKU不存在: id=" + skuId));
        }
        if (skuCode != null && !skuCode.isBlank()) {
            return skuRepository
                    .findByCode(tenantId, skuCode)
                    .orElseThrow(() -> BusinessException.notFound("SKU不存在: code=" + skuCode));
        }
        throw BusinessException.badRequest("skuId 或 skuCode 必须提供一个");
    }

    public List<SkuDTO> findByOwner(Long ownerId) {
        return skuRepository.findByOwner(UserContext.getTenantId(), ownerId).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    public IPage<SkuDTO> page(SkuPageQuery query) {
        Page<Sku> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Sku> result =
                skuMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<Sku>()
                                .eq(Sku::getTenantId, UserContext.getTenantId())
                                .eq(query.getOwnerId() != null, Sku::getOwnerId, query.getOwnerId())
                                .eq(query.getSkuCode() != null, Sku::getSkuCode, query.getSkuCode())
                                .like(
                                        query.getSkuName() != null,
                                        Sku::getSkuName,
                                        query.getSkuName())
                                .eq(
                                        query.getCategory() != null,
                                        Sku::getCategory,
                                        query.getCategory())
                                .eq(
                                        query.getBatchManaged() != null,
                                        Sku::getBatchManaged,
                                        query.getBatchManaged()));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public SkuDTO create(SkuCreateCmd cmd) {
        Sku sku = assembler.toEntity(cmd);
        sku.setTenantId(UserContext.getTenantId());
        sku.setCreatedBy(UserContext.getUserId());
        sku.setUpdatedBy(UserContext.getUserId());
        domainService.validateCreate(sku);
        skuRepository.save(sku);
        return assembler.toDTO(sku);
    }

    @Transactional
    public SkuDTO update(SkuUpdateCmd cmd) {
        Sku sku =
                skuRepository
                        .findById(cmd.getId())
                        .orElseThrow(() -> BusinessException.notFound("SKU不存在"));
        assembler.mergeToEntity(cmd, sku);
        sku.setUpdatedBy(UserContext.getUserId());
        domainService.validateUpdate(sku);
        skuRepository.update(sku);
        return assembler.toDTO(sku);
    }

    @Transactional
    public void delete(Long id) {
        skuRepository.deleteById(id);
    }

    @Transactional
    public void disable(Long id) {
        Sku sku =
                skuRepository.findById(id).orElseThrow(() -> BusinessException.notFound("SKU不存在"));
        domainService.disableSku(sku);
        skuRepository.update(sku);
    }

    @Transactional
    public void enable(Long id) {
        Sku sku =
                skuRepository.findById(id).orElseThrow(() -> BusinessException.notFound("SKU不存在"));
        domainService.enableSku(sku);
        skuRepository.update(sku);
    }

    // ───── 包装规格管理 ─────

    public List<SkuPackageDTO> listPackages(Long skuId) {
        return packageRepository.findBySkuId(UserContext.getTenantId(), skuId).stream()
                .map(assembler::toPackageDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkuPackageDTO createPackage(SkuPackageCreateCmd cmd) {
        SkuPackage pkg = assembler.toEntity(cmd);
        pkg.setTenantId(UserContext.getTenantId());
        pkg.setCreatedBy(UserContext.getUserId());
        pkg.setUpdatedBy(UserContext.getUserId());
        domainService.validatePackageCreate(pkg);
        packageRepository.save(pkg);
        return assembler.toPackageDTO(pkg);
    }

    @Transactional
    public void deletePackage(Long id) {
        packageRepository.deleteById(id);
    }
}
