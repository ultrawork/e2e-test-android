package com.ultrawork.notes.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryTest {

    @Test
    fun `Category has all required fields with String type`() {
        val category = Category(
            id = "cat-1",
            name = "Work",
            color = "#FF5733",
            createdAt = "2024-01-01T00:00:00Z"
        )

        assertEquals("cat-1", category.id)
        assertEquals("Work", category.name)
        assertEquals("#FF5733", category.color)
        assertEquals("2024-01-01T00:00:00Z", category.createdAt)
    }

    @Test
    fun `Category data class supports equality`() {
        val a = Category(id = "1", name = "Home", color = "#000", createdAt = "2024-01-01")
        val b = Category(id = "1", name = "Home", color = "#000", createdAt = "2024-01-01")

        assertEquals(a, b)
    }

    @Test
    fun `Category data class supports copy`() {
        val original = Category(id = "1", name = "Home", color = "#000", createdAt = "2024-01-01")
        val copy = original.copy(name = "Office")

        assertEquals("1", copy.id)
        assertEquals("Office", copy.name)
        assertEquals("#000", copy.color)
        assertEquals("2024-01-01", copy.createdAt)
    }
}
