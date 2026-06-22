package com.wms.masterdata.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.masterdata.application.assembler.WarehouseAssembler;
import com.wms.masterdata.application.dto.WarehouseCreateCmd;
import com.wms.masterdata.application.dto.WarehouseDTO;
import com.wms.masterdata.application.dto.WarehousePageQuery;
import com.wms.masterdata.application.dto.WarehouseUpdateCmd;
import com.wms.masterdata.domain.entity.Warehouse;
import com.wms.masterdata.domain.repository.WarehouseRepository;
import com.wms.masterdata.domain.service.WarehouseDomainService;
import com.wms.masterdata.infrastructure.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库应用服务 —— 用例编排 + 事务边界。
 * <p>
 * 应用层只做编排，不写业务规则（规则在 DomainService 中）。
 */
@Service
@RequiredArgsConstructor
public class WarehouseAppService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseDomainService domainService;
    private final WarehouseAssembler assembler;

    // ───── 查询（只读，不需要事务） ─────

    public WarehouseDTO findById(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("仓库不存在"));
        return assembler.toDTO(w);
    }

    public List<WarehouseDTO> findAll() {
Long tenantId = UserContext.getTenantId();
        return warehouseRepository.findByStatus(tenantId, 1).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    public IPage<WarehouseDTO> page(WarehousePageQuery query) {
Long tenantId = UserContext.getTenantId();
        Page<Warehouse> page = new Page<>(query.getPageNum(), query.getPageSize());

        IPage<Warehouse> result = warehouseMapper.selectPage(page,
                new LambdaQueryWrapper<Warehouse>()
                        .eq(Warehouse::getTenantId, tenantId)
                        .eq(query.getWhCode() != null, Warehouse::getWhCode, query.getWhCode())
                        .like(query.getWhName() != null, Warehouse::getWhName, query.getWhName())
                        .eq(query.getWhType() != null, Warehouse::getWhType, query.getWhType())
                        .eq(query.getStatus() != null, Warehouse::getStatus, query.getStatus()));

        return result.convert(assembler::toDTO);
    }

    // ───── 写操作（事务边界） ─────

    @Transactional
    public WarehouseDTO create(WarehouseCreateCmd cmd) {
        Warehouse warehouse = assembler.toEntity(cmd);
        // 注入上下文
Long tenantId = UserContext.getTenantId();
Long userId = UserContext.getUserId();
        warehouse.setTenantId(tenantId);
        warehouse.setCreatedBy(userId);
        warehouse.setUpdatedBy(userId);

        // 领域规则校验
        domainService.validateCreate(warehouse);

        // 持久化
        warehouseRepository.save(warehouse);

        return assembler.toDTO(warehouse);
    }

    @Transactional
    public WarehouseDTO update(WarehouseUpdateCmd cmd) {
        Warehouse warehouse = warehouseRepository.findById(cmd.getId())
                .orElseThrow(() -> BusinessException.notFound("仓库不存在"));

        assembler.mergeToEntity(cmd, warehouse);
        warehouse.setUpdatedBy(UserContext.getUserId());

        domainService.validateUpdate(warehouse);
        warehouseRepository.update(warehouse);

        return assembler.toDTO(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("仓库不存在"));
        // 删除前检查：后续可对接 Inventory 域判断是否有库存
        warehouseRepository.deleteById(id);
    }

    @Transactional
    public void disable(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("仓库不存在"));
        domainService.disableWarehouse(warehouse);
        warehouseRepository.update(warehouse);
    }

    @Transactional
    public void enable(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("仓库不存在"));
        domainService.enableWarehouse(warehouse);
        warehouseRepository.update(warehouse);
    }
}
