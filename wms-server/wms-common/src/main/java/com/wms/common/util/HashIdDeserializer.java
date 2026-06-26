package com.wms.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wms.common.annotation.HashId;

import java.io.IOException;

/**
 * Long 类型反序列化器：ID 字段用 Hashids 解码，非 ID 字段按标准 Long 解析。
 *
 * <p>ID 字段识别规则与 {@link HashIdSerializer} 一致：
 *
 * <ol>
 *   <li>标注了 {@link HashId} 注解
 *   <li>字段名为 {@code id}
 *   <li>字段名以 {@code Id} 结尾
 * </ol>
 *
 * <p>向后兼容：若 hashid 解码失败，回退 {@code Long.parseLong}（过渡期内前端可能仍传原始数字）。
 */
public class HashIdDeserializer extends StdDeserializer<Long> implements ContextualDeserializer {

    private final HashIdsUtil hashIdsUtil;
    private final boolean idField;

    /** 由 JacksonConfig 通过 SimpleModule 注册时使用（默认启用 ID 识别）。 */
    public HashIdDeserializer(HashIdsUtil hashIdsUtil) {
        super(Long.class);
        this.hashIdsUtil = hashIdsUtil;
        this.idField = true;
    }

    private HashIdDeserializer(HashIdsUtil hashIdsUtil, boolean idField) {
        super(Long.class);
        this.hashIdsUtil = hashIdsUtil;
        this.idField = idField;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        if (property != null && isIdField(property)) {
            return new HashIdDeserializer(hashIdsUtil, true);
        }
        // 非 ID 的 Long 字段：跳过 hashid 解码，按标准 Long 解析
        return new HashIdDeserializer(hashIdsUtil, false);
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (!idField) {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            return p.getValueAsLong();
        }
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        String text = p.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        // 先尝试 Hashids 解码
        Long decoded = hashIdsUtil.decodeSingle(text);
        if (decoded != null) {
            return decoded;
        }
        // 回退：过渡期前端可能仍传原始数字
        return Long.parseLong(text);
    }

    private boolean isIdField(BeanProperty property) {
        if (property.getAnnotation(HashId.class) != null) {
            return true;
        }
        String name = property.getName();
        return "id".equals(name) || name.endsWith("Id");
    }
}
