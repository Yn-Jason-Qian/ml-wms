package com.wms.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wms.common.annotation.HashId;

import java.io.IOException;

/**
 * Long 类型序列化器：ID 字段用 Hashids 编码，非 ID 字段回退 ToStringSerializer。
 *
 * <p>ID 字段识别规则（满足任一即视为 ID）：
 *
 * <ol>
 *   <li>标注了 {@link HashId} 注解
 *   <li>字段名为 {@code id}
 *   <li>字段名以 {@code Id} 结尾（如 {@code warehouseId}）
 * </ol>
 */
public class HashIdSerializer extends StdSerializer<Long> implements ContextualSerializer {

    private final HashIdsUtil hashIdsUtil;

    /** 由 JacksonConfig 通过 SimpleModule 注册时使用。 */
    public HashIdSerializer(HashIdsUtil hashIdsUtil) {
        super(Long.class);
        this.hashIdsUtil = hashIdsUtil;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null && isIdField(property)) {
            return this;
        }
        // 非 ID 的 Long 字段回退原有行为（防止 JS 精度丢失）
        return ToStringSerializer.instance;
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(hashIdsUtil.encode(value));
    }

    private boolean isIdField(BeanProperty property) {
        if (property.getAnnotation(HashId.class) != null) {
            return true;
        }
        String name = property.getName();
        return "id".equals(name) || name.endsWith("Id");
    }
}
