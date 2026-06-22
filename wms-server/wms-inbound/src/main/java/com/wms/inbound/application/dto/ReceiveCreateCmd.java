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
    @NotNull private Long skuId;
    @NotNull private BigDecimal receiveQty;
    private Long asnLineId;
    private String receivePackage;
    @NotNull private Long receiveLocationId;
    private String batchNo;
    private String lotAttrs;
    private LocalDate productionDate;
    private LocalDate expiryDate;
}
