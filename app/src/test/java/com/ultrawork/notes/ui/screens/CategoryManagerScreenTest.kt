package com.ultrawork.notes.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryManagerScreenTest {

    @Test
    fun `isValidHexColor returns true for valid #RRGGBB value`() {
        assertTrue(isValidHexColor("#A1B2C3"))
    }

    @Test
    fun `isValidHexColor returns false for invalid value`() {
        assertFalse(isValidHexColor("123456"))
        assertFalse(isValidHexColor("#FFF"))
        assertFalse(isValidHexColor("#GGGGGG"))
    }

    @Test
    fun `parseHexColorOrDefault parses valid hex color`() {
        assertEquals(Color(0xFF112233), parseHexColorOrDefault("#112233"))
    }

    @Test
    fun `parseHexColorOrDefault returns default for invalid hex`() {
        val fallback = Color(0xFFABCDEF)
        assertEquals(fallback, parseHexColorOrDefault("#XYZXYZ", fallback))
    }
}
