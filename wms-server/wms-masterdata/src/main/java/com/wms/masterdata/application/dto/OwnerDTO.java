package com.wms.masterdata.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OwnerDTO {
    private Long id;
    private String ownerCode;
    private String ownerName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
