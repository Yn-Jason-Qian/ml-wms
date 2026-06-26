package com.wms.common.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ID 混淆工具：将 Long ID 编码为短字符串，并可通过盐值逆向解码。
 *
 * <p>算法：XOR 混淆 + Base62 编码。将 64-bit Long 先与盐值派生的密钥做 XOR 混淆， 再转为 Base62 短字符串。解码时反向操作。
 *
 * <p>不是加密算法，但比纯 Base62 多了盐值带来的混淆性，适用于 WMS 内部系统。 字符串长度：64-bit 最多 11 个字符（相比原始 18 位数字缩短约 40%）。
 */
@Slf4j
@Component
public class HashIdsUtil {

    /** Base62 字符集（数字 + 大写 + 小写，URL 安全） */
    private static final String BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final long xorKey;

    public HashIdsUtil(
            @Value("${hashids.salt}") String salt,
            @Value("${hashids.min-length:8}") int minLength) {
        this.xorKey = deriveKey(salt);
    }

    /** 将 Long ID 编码为混淆后的 Base62 字符串。 */
    public String encode(Long id) {
        if (id == null) {
            return null;
        }
        long xored = id ^ xorKey;
        return toBase62(xored);
    }

    /** 将混淆字符串解码为原始 Long ID。解码失败返回 null。 */
    public Long decodeSingle(String hashid) {
        if (hashid == null || hashid.isBlank()) {
            return null;
        }
        try {
            long xored = fromBase62(hashid.trim());
            return xored ^ xorKey;
        } catch (Exception e) {
            log.debug("Failed to decode hashid '{}': {}", hashid, e.getMessage());
            return null;
        }
    }

    /** 从盐值派生 64-bit XOR 密钥（SHA-256 取低 64 位）。 */
    private static long deriveKey(String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(salt.getBytes(StandardCharsets.UTF_8));
            long key = 0;
            for (int i = 0; i < 8; i++) {
                key = (key << 8) | (hash[i] & 0xFF);
            }
            return key != 0 ? key : 0x5DEECE66DL; // 确保非零
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** 将 long（作为无符号数）转为 Base62 字符串。 */
    private static String toBase62(long num) {
        if (num == 0) {
            return String.valueOf(BASE62.charAt(0));
        }
        StringBuilder sb = new StringBuilder(11);
        long n = num;
        while (n != 0) {
            int remainder = (int) Long.remainderUnsigned(n, 62);
            sb.append(BASE62.charAt(remainder));
            n = Long.divideUnsigned(n, 62);
        }
        return sb.reverse().toString();
    }

    /** 将 Base62 字符串转为 long（无符号解析）。 */
    private static long fromBase62(String str) {
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int digit = BASE62.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base62 char: " + c);
            }
            result = result * 62 + digit;
        }
        return result;
    }
}
