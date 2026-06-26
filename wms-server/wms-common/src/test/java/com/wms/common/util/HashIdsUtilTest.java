package com.wms.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID 混淆工具单元测试。 */
class HashIdsUtilTest {

    private HashIdsUtil hashIdsUtil;

    @BeforeEach
    void setUp() {
        hashIdsUtil = new HashIdsUtil("test-salt", 8);
    }

    @Test
    void testEncodeDecodeRoundtrip() {
        // 64-bit Snowflake ID
        Long original = 185432345678901248L;
        String encoded = hashIdsUtil.encode(original);
        assertNotNull(encoded);
        assertEquals(original, hashIdsUtil.decodeSingle(encoded));
    }

    @Test
    void testEncodeShorterThanOriginal() {
        // 18 位 Snowflake ID → hashid 应该更短（最多 11 字符）
        Long id = 185432345678901248L;
        String encoded = hashIdsUtil.encode(id);
        assertTrue(
                encoded.length() <= 11,
                "Expected <=11 chars, got " + encoded.length() + ": " + encoded);
        assertTrue(
                encoded.length() < String.valueOf(id).length(),
                "Hashid should be shorter than original 18-digit number");
    }

    @Test
    void testEncodeNull() {
        assertNull(hashIdsUtil.encode(null));
    }

    @Test
    void testDecodeNullAndEmpty() {
        assertNull(hashIdsUtil.decodeSingle(null));
        assertNull(hashIdsUtil.decodeSingle(""));
        assertNull(hashIdsUtil.decodeSingle("   "));
    }

    @Test
    void testDecodeInvalidString() {
        // 无效字符串（含非法 Base62 字符）返回 null
        assertNull(hashIdsUtil.decodeSingle("!@#$%^&*"));
    }

    @Test
    void testDifferentSaltsProduceDifferentOutput() {
        HashIdsUtil otherUtil = new HashIdsUtil("different-salt", 8);
        Long id = 123L;
        assertNotEquals(hashIdsUtil.encode(id), otherUtil.encode(id));
    }

    @Test
    void testSmallId() {
        // 小 ID（1, 2, 3...）正常加解密
        for (long i = 1; i <= 100; i++) {
            String encoded = hashIdsUtil.encode(i);
            assertNotNull(encoded);
            assertEquals(i, hashIdsUtil.decodeSingle(encoded));
        }
    }

    @Test
    void testNegativeId() {
        // 负数 ID（XOR 可能产生负数）正常加解密
        Long id = -1L;
        String encoded = hashIdsUtil.encode(id);
        assertEquals(id, hashIdsUtil.decodeSingle(encoded));
    }

    @Test
    void testMaxLongId() {
        // Long.MAX_VALUE 正常加解密
        Long id = Long.MAX_VALUE;
        String encoded = hashIdsUtil.encode(id);
        assertEquals(id, hashIdsUtil.decodeSingle(encoded));
    }

    @Test
    void testDeterministic() {
        // 相同输入产生相同输出
        Long id = 123456789L;
        String enc1 = hashIdsUtil.encode(id);
        String enc2 = hashIdsUtil.encode(id);
        assertEquals(enc1, enc2);
    }
}
