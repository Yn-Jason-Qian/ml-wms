package com.wms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 通用创建结果 DTO。用于 Controller 中 create* 方法的返回值，替代 Map.of("xxxNo", ..., "xxxId", ...)。 */
@Data
@AllArgsConstructor
public class CreateResultDTO {
    private String no;
    private Long id;
}
