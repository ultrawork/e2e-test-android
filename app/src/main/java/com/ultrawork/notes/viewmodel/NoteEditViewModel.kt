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

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesApiService: NotesApiService
) : ViewModel() {

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    init {
        if (noteId.isNotEmpty() && noteId != "new") {
            loadNote()
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                _note.value = notesApiService.getNoteById(noteId)
            } catch (_: Exception) {
                // Note loading failed — screen will behave as new note
            }
        }
    }
}
