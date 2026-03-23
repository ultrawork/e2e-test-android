package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.FakeNotesRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultServiceLocatorTest {

    private val locator = DefaultServiceLocator()

    @Test
    fun `provideNotesRepository returns FakeNotesRepository when apiBaseUrl is empty`() {
        val repo = locator.provideNotesRepository("")
        assertTrue(repo is FakeNotesRepository)
    }

    @Test
    fun `provideNotesRepository returns FakeNotesRepository when apiBaseUrl is blank`() {
        val repo = locator.provideNotesRepository("   ")
        assertTrue(repo is FakeNotesRepository)
    }

    @Test
    fun `provideNotesRepository returns repository when apiBaseUrl is non-blank`() {
        val repo = locator.provideNotesRepository("http://localhost:3000/api")
        assertNotNull(repo)
    }

    private fun assertNotNull(obj: Any?) {
        assertTrue(obj != null)
    }
}
