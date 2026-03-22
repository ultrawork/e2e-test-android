package com.ultrawork.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteEditViewModelTest {
    @Test
    fun `save with blank title emits validation error`() = runBlocking {
        val viewModel = NoteEditViewModel(SavedStateHandle())

        viewModel.onTitleChange("   ")
        viewModel.save()

        assertEquals(
            NoteEditViewModel.SaveResult.ValidationError("Note title must not be empty"),
            viewModel.saveResults.first()
        )
    }
}
