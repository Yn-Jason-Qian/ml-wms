package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Warehouse.WhType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarehouseDTO {

    private Long id;
    private String whCode;
    private String whName;
    private WhType whType;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
