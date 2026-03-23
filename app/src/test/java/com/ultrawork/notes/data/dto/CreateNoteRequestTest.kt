package com.ultrawork.notes.data.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreateNoteRequestTest {

    @Test
    fun `creates request with all fields`() {
        val request = CreateNoteRequest(
            title = "Test Note",
            content = "Test Content",
            categoryIds = listOf("cat-1", "cat-2")
        )

        assertEquals("Test Note", request.title)
        assertEquals("Test Content", request.content)
        assertEquals(listOf("cat-1", "cat-2"), request.categoryIds)
    }

    @Test
    fun `categoryIds defaults to null`() {
        val request = CreateNoteRequest(
            title = "Test Note",
            content = "Test Content"
        )

        assertNull(request.categoryIds)
    }

    @Test
    fun `supports data class equality`() {
        val a = CreateNoteRequest("Title", "Content", listOf("1"))
        val b = CreateNoteRequest("Title", "Content", listOf("1"))

        assertEquals(a, b)
    }

    @Test
    fun `supports copy with modified fields`() {
        val original = CreateNoteRequest("Title", "Content")
        val copy = original.copy(title = "New Title")

        assertEquals("New Title", copy.title)
        assertEquals("Content", copy.content)
        assertNull(copy.categoryIds)
    }
}
