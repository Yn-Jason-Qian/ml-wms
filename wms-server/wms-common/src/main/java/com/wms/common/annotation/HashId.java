package com.wms.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Long 字段需通过 Hashids 加解密。
 *
 * <p>命名规则自动识别（字段名 {@code id} 或以 {@code Id} 结尾）已覆盖绝大多数场景， 此注解仅用于不遵循命名规则的 ID 字段（如 {@code
 * assignTo}、{@code freezeBy}）。
 *
 * <p>序列化（Long → hashid 字符串）和反序列化（hashid 字符串 → Long）均通过 {@link com.wms.common.util.HashIdSerializer}
 * / {@link com.wms.common.util.HashIdDeserializer} 处理。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HashId {}
