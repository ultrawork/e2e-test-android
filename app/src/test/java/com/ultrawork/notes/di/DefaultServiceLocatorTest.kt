package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.FakeNotesRepository
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultServiceLocatorTest {

    @Test
    fun `provideNotesRepository returns FakeNotesRepository when apiBaseUrl is empty`() {
        val locator = DefaultServiceLocator("")
        val repo = locator.provideNotesRepository()
        assertTrue(repo is FakeNotesRepository)
    }

    @Test
    fun `provideNotesRepository returns FakeNotesRepository when apiBaseUrl is blank`() {
        val locator = DefaultServiceLocator("   ")
        val repo = locator.provideNotesRepository()
        assertTrue(repo is FakeNotesRepository)
    }

    @Test
    fun `provideNotesRepository returns NotesRepositoryImpl when apiBaseUrl is non-blank`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        val repo = locator.provideNotesRepository()
        assertTrue(repo is NotesRepositoryImpl)
    }

    @Test
    fun `provideNotesRepository returns non-null for any input`() {
        val locator = DefaultServiceLocator("http://localhost:3000/api/")
        assertNotNull(locator.provideNotesRepository())
    }
}
