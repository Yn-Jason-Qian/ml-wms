package com.wms.print.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_print_record")
public class PrintRecord extends BaseEntity {
    private Long templateId;
    private String printerName;
    private String printContent;
    private Integer printCount;
    private String refType;
    private Long refId;
    private Long printBy;
    private LocalDateTime printAt;
}
