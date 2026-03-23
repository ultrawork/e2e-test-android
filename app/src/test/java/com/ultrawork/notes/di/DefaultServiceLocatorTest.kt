package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultServiceLocatorTest {

    @Test
    fun `notesRepository returns NotesRepositoryImpl`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        assertTrue(locator.notesRepository is NotesRepositoryImpl)
    }

    @Test
    fun `notesRepository returns non-null`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        assertNotNull(locator.notesRepository)
    }
}
