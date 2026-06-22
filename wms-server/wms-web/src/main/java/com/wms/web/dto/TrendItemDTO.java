package com.wms.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendItemDTO {
    private String date;
    private BigDecimal inboundQty;
    private BigDecimal outboundQty;
}
