package com.example.mynumbercardidp.keycloak.util;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilTest {
    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("abc123"));
    }

    @Test
    public void testIsNonEmpty() {
        assertTrue(StringUtil.isNonEmpty("abc123"));
        assertFalse(StringUtil.isNonEmpty(""));
        assertFalse(StringUtil.isNonEmpty(null));
    }

    @ParameterizedTest
    @CsvSource({
        "abc123, Abc123",
        "123456, 123456",
        "$12.34, $12.34"
    })
    public void testToFirstUpperCase(String input, String expected) {
        assertEquals(expected, StringUtil.toFirstUpperCase(input));
    }
}
