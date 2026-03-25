package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(notes, _searchQuery) { notesList, query ->
        if (query.isBlank()) {
            notesList
        } else {
            notesList.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Loads all notes from the repository. */
    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getNotes()
                .onSuccess { _notes.value = it }
                .onFailure { _error.value = it.message ?: "Failed to load notes" }
            _isLoading.value = false
        }
    }

    /** Creates a new note with the given title (content = title per spec). */
    fun createNote(title: String) {
        viewModelScope.launch {
            _error.value = null
            repository.createNote(title)
                .onSuccess { loadNotes() }
                .onFailure { _error.value = it.message ?: "Failed to create note" }
        }
    }

    /** Deletes a note by its id. */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            _error.value = null
            repository.deleteNote(id)
                .onSuccess { _notes.value = _notes.value.filter { it.id != id } }
                .onFailure { _error.value = it.message ?: "Failed to delete note" }
        }
    }
}
