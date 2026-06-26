package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Warehouse.WhType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarehouseUpdateCmd {

    @NotNull(message = "仓库ID不能为空")
    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    @Size(min = 5, max = 19)
    private String whCode;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 128)
    private String whName;

    @NotNull(message = "仓库类型不能为空")
    private WhType whType;

    private String address;
    private String contactPerson;
    private String contactPhone;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
}
