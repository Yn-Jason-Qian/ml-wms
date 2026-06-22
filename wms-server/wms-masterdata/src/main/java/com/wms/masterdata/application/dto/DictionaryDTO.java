package com.wms.masterdata.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictionaryDTO {
    private Long id;
    private String dictType;
    private String dictCode;
    private String dictName;
    private String parentCode;
    private Integer sortOrder;
    private String extra;
    private Integer status;
    private LocalDateTime createdAt;
}
