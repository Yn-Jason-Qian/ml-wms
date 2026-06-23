package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsnCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long ownerId;
    @NotBlank private String asnType;
    private String sourceNo;
    private LocalDateTime expectedArriveTime;
    private String carrierName;
    private String carrierPhone;
    private String remark;
    private List<AsnLineItem> lines;

    @Data
    public static class AsnLineItem {
        private Long skuId;           // 可选: SKU ID
        private String skuCode;       // 可选: SKU 编码 (PDA扫码场景)
        @NotNull private java.math.BigDecimal expectedQty;
        private String batchNo;
        private String lotAttrs;
        private java.time.LocalDate productionDate;
        private java.time.LocalDate expiryDate;
    }
}
