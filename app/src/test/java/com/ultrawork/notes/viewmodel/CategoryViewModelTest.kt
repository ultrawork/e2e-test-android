package com.ultrawork.notes.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryViewModelTest {
    @Test
    fun `create with blank name sets error`() {
        val viewModel = CategoryViewModel()

        viewModel.create("   ", "#FFFFFF")

        assertEquals("Category name must not be empty", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.categories.isEmpty())
    }

    @Test
    fun `create with invalid color sets error`() {
        val viewModel = CategoryViewModel()

        viewModel.create("Work", "FFFFFF")

        assertEquals("Category color must be a valid hex", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.categories.isEmpty())
    }
}
