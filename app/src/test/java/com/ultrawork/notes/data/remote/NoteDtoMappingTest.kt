package com.ultrawork.notes.data.remote

import com.ultrawork.notes.data.remote.dto.NoteDto
import com.ultrawork.notes.data.remote.dto.toNote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Date

class NoteDtoMappingTest {

    @Test
    fun `toNote maps all fields correctly`() {
        val now = Date()
        val dto = NoteDto(
            id = "abc-123",
            title = "Test Title",
            content = "Test Content",
            userId = "user-1",
            createdAt = now,
            updatedAt = now
        )

        val note = dto.toNote()

        assertEquals("abc-123", note.id)
        assertEquals("Test Title", note.title)
        assertEquals("Test Content", note.content)
        assertEquals("user-1", note.userId)
        assertFalse(note.isFavorited)
        assertEquals(now, note.createdAt)
        assertEquals(now, note.updatedAt)
    }

    @Test
    fun `toNote sets isFavorited to false`() {
        val dto = NoteDto(
            id = "1",
            title = "T",
            content = "C",
            userId = "u",
            createdAt = Date(),
            updatedAt = Date()
        )

        assertFalse(dto.toNote().isFavorited)
    }
}
