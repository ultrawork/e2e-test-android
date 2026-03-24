package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultServiceLocatorTest {

    @Test
    fun `notesRepository returns NotesRepositoryImpl when apiBaseUrl is non-blank`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        assertTrue(locator.notesRepository is NotesRepositoryImpl)
    }

    @Test
    fun `notesRepository returns null when apiBaseUrl is empty`() {
        val locator = DefaultServiceLocator("")
        assertNull(locator.notesRepository)
    }

    @Test
    fun `notesRepository returns null when apiBaseUrl is blank`() {
        val locator = DefaultServiceLocator("   ")
        assertNull(locator.notesRepository)
    }

    @Test
    fun `notesRepository returns same instance on repeated calls`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        val first = locator.notesRepository
        val second = locator.notesRepository
        assertNotNull(first)
        assertSame(first, second)
    }
}
