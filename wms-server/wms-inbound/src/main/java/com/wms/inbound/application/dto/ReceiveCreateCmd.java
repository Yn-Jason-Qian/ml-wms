package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReceiveCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long ownerId;
    private Long asnHeaderId;
    private String receiveType;
    private Long skuId;               // 可选: SKU ID
    private String skuCode;           // 可选: SKU 编码 (PDA扫码场景)
    @NotNull private BigDecimal receiveQty;
    private Long asnLineId;
    private String receivePackage;
    private Long receiveLocationId;   // 可选: 库位 ID
    private String receiveLocationCode; // 可选: 库位编码 (PDA扫码场景)
    private String batchNo;
    private String lotAttrs;
    private LocalDate productionDate;
    private LocalDate expiryDate;
}
