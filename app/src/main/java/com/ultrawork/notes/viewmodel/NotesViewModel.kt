package com.ultrawork.notes.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

/**
 * ViewModel for notes list screen. Loads, creates, and deletes notes via repository.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

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

    /** Loads all notes from the API. Shows empty list on failure. */
    fun loadNotes() {
        viewModelScope.launch {
            isLoading = true
            error = null
            _notes.value = repo.getNotes()
            isLoading = false
        }
    }

    /** Creates a new note via API and appends it to the local list. */
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            error = null
            runCatching {
                val note = repo.addNote(title, content)
                _notes.value = _notes.value + note
            }.onFailure { e ->
                error = e.message
            }
        }
    }

    /** Deletes a note via API and removes it from the local list. */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            error = null
            runCatching {
                repo.removeNote(id)
                _notes.value = _notes.value.filterNot { it.id == id }
            }.onFailure { e ->
                error = e.message
            }
        }
    }
}
