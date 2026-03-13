package com.ultrawork.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.data.remote.NotesApiService
import com.ultrawork.notes.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NoteUiState {
    data object Loading : NoteUiState()
    data class Success(val note: Note) : NoteUiState()
    data class Error(val message: String) : NoteUiState()
    data object NewNote : NoteUiState()
}

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesApiService: NotesApiService
) : ViewModel() {

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    private val _uiState = MutableStateFlow<NoteUiState>(
        if (noteId.isNotEmpty() && noteId != "new") NoteUiState.Loading else NoteUiState.NewNote
    )
    val uiState: StateFlow<NoteUiState> = _uiState

    init {
        if (noteId.isNotEmpty() && noteId != "new") {
            loadNote()
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val note = notesApiService.getNoteById(noteId)
                _uiState.value = NoteUiState.Success(note)
            } catch (e: Exception) {
                _uiState.value = NoteUiState.Error(e.message ?: "Failed to load note")
            }
        }
    }

    fun retry() {
        _uiState.value = NoteUiState.Loading
        loadNote()
    }
}
