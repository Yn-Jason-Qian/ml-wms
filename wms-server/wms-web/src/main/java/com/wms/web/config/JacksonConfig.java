package com.wms.web.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置：将 Long 类型 ID 序列化为字符串。
 * <p>
 * 原因：后端使用雪花算法生成 ID（Long），其值超过 JavaScript Number 的安全整数范围
 *（2^53 -1）。如果直接以 JSON 数字返回，前端解析时会丢失精度，导致基于 ID 的
 * 编辑、删除、关联查询等操作出现 404/400 错误。统一把 Long 序列化为字符串可避免该问题。
 * <p>
 * 注意：只序列化 Long 对象类型（如实体 ID），不序列化 long 基本类型（如 PageResponse.total 计数），
 * 否则分页组件的 total 字段会变成字符串导致分页失效。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public com.fasterxml.jackson.databind.Module longAsStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }
}
