package com.ultrawork.notes.data.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateNoteRequestTest {

    @Test
    fun `creates request with all fields`() {
        val request = UpdateNoteRequest(
            title = "Updated Note",
            content = "Updated Content",
            categoryIds = listOf("cat-3")
        )

        assertEquals("Updated Note", request.title)
        assertEquals("Updated Content", request.content)
        assertEquals(listOf("cat-3"), request.categoryIds)
    }

    @Test
    fun `categoryIds defaults to null`() {
        val request = UpdateNoteRequest(
            title = "Updated Note",
            content = "Updated Content"
        )

        assertNull(request.categoryIds)
    }

    @Test
    fun `supports data class equality`() {
        val a = UpdateNoteRequest("Title", "Content", listOf("1"))
        val b = UpdateNoteRequest("Title", "Content", listOf("1"))

        assertEquals(a, b)
    }

    @Test
    fun `supports copy with modified fields`() {
        val original = UpdateNoteRequest("Title", "Content", listOf("1"))
        val copy = original.copy(categoryIds = null)

        assertEquals("Title", copy.title)
        assertEquals("Content", copy.content)
        assertNull(copy.categoryIds)
    }
}
