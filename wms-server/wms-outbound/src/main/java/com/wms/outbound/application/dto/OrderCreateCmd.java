package com.wms.outbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long ownerId;
    @NotBlank private String orderType;
    private String sourceNo;
    private String customerName;
    private String customerAddress;
    private LocalDateTime expectedShipTime;
    private Integer priority;
    private List<LineItem> lines;

    @Data
    public static class LineItem {
        private Long skuId;             // 可选: SKU ID
        private String skuCode;         // 可选: SKU 编码 (PDA扫码场景)
        @NotNull private BigDecimal orderQty;
        private String batchNo;
        private String lotAttrs;
    }
}
