package com.wms.common.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 单据号生成工具。
 *
 * <p>格式：{@code 前缀-yyyyMMddHHmmss-XXXX}，后缀为 4 位随机字符。
 *
 * <p>原先各域直接使用「前缀 + 秒级时间戳」，同一秒内创建两张同类型单据会命中数据库唯一键 （如 {@code uk_tenant_move_no}），批量建单或多台 PDA
 * 同时作业时必然冲突，故追加随机后缀。
 */
public final class DocNoUtil {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 去掉易混淆字符（I / L / O）的编码表 */
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private DocNoUtil() {}

    /** 生成单据号，如 {@code MV-20260911105557-8K3D}。 */
    public static String next(String prefix) {
        StringBuilder sb =
                new StringBuilder(prefix.length() + 19)
                        .append(prefix)
                        .append('-')
                        .append(LocalDateTime.now().format(TIMESTAMP))
                        .append('-');
        for (int i = 0; i < 4; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
