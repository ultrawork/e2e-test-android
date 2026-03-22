package com.ultrawork.notes.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryManagerScreenTest {

    @Test
    fun `isValidHexColor returns true for valid color`() {
        assertTrue(isValidHexColor("#A1B2C3"))
    }

    @Test
    fun `isValidHexColor returns false for invalid color`() {
        assertEquals(false, isValidHexColor("A1B2C3"))
        assertEquals(false, isValidHexColor("#FFF"))
    }

    @Test
    fun `parseHexColorOrNull parses valid hex`() {
        assertEquals(Color(0xFFA1B2C3), parseHexColorOrNull("#A1B2C3"))
    }

    @Test
    fun `parseHexColorOrNull returns null for invalid hex`() {
        assertNull(parseHexColorOrNull("#ZZZZZZ"))
    }
}
