package com.wms.common.base;

import com.wms.common.annotation.HashId;

import lombok.Data;

/**
 * 可选基类 —— 为 DTO 提供带 {@link HashId} 注解的 {@code id} 字段。
 *
 * <p>继承此类的 DTO 无需再声明 {@code id} 字段，且会自动获得 Hashids 加解密能力。 命名规则已自动覆盖字段名 {@code id}，此处的
 * {@code @HashId} 为显式标记和文档用途。
 */
@Data
public abstract class IdDto {
    @HashId private Long id;
}
