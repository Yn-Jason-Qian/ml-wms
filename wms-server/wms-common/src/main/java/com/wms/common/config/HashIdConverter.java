package com.wms.common.config;

import com.wms.common.util.HashIdsUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * URL 路径变量 / 查询参数的 String → Long 转换器。
 *
 * <p>处理 {@code @PathVariable Long id} 和 {@code @RequestParam Long} 的转换。 识别规则：包含字母 → Hashids 解码；纯数字
 * → Long.parseLong（向后兼容过渡期内的原始 ID）。
 *
 * <p>注册方式：在 {@link WebMvcConfig#addFormatters} 中注册。
 */
@Component
@RequiredArgsConstructor
public class HashIdConverter implements Converter<String, Long> {

    private final HashIdsUtil hashIdsUtil;

    @Override
    public Long convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        // 包含字母 → hashid 字符串
        if (source.matches(".*[a-zA-Z].*")) {
            return hashIdsUtil.decodeSingle(source);
        }
        // 纯数字 → 原始 Long（向后兼容）
        return Long.parseLong(source);
    }
}
