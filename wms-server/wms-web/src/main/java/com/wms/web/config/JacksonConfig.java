package com.wms.web.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wms.common.util.HashIdDeserializer;
import com.wms.common.util.HashIdSerializer;
import com.wms.common.util.HashIdsUtil;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置：Long 类型 ID 字段使用 Hashids 加解密，枚举类型序列化为字符串。
 *
 * <p>ID 识别规则（满足任一即视为需要 Hashids 处理的 ID 字段）：
 *
 * <ol>
 *   <li>标注了 {@link com.wms.common.annotation.HashId} 注解
 *   <li>字段名为 {@code id}
 *   <li>字段名以 {@code Id} 结尾（如 {@code warehouseId}）
 * </ol>
 *
 * <p>非 ID 的 Long 字段保持原有 {@code ToStringSerializer} 行为，避免 JS 精度丢失。
 *
 * <p>{@code long} 基本类型（如 PageResponse.total）不受影响，继续序列化为 JSON 数字。
 *
 * <p>枚举类型全局配置为按 {@code toString()} 序列化（默认即 {@code name()}），与数据库 VARCHAR 存储值一致。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public com.fasterxml.jackson.databind.Module hashIdModule(HashIdsUtil hashIdsUtil) {
        SimpleModule module = new SimpleModule("HashIdModule");
        module.addSerializer(Long.class, new HashIdSerializer(hashIdsUtil));
        module.addDeserializer(Long.class, new HashIdDeserializer(hashIdsUtil));
        return module;
    }

    /** 全局枚举序列化为字符串（使用 {@code Enum.toString()}，默认等于 {@code Enum.name()}）。 */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer enumCustomizer() {
        return builder ->
                builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
