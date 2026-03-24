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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filtered notes combining raw notes, search query, and favorites filter. */
    val filteredNotes: StateFlow<List<Note>> = combine(
        _notes, _searchQuery, _showFavoritesOnly
    ) { notesList, query, favoritesOnly ->
        notesList
            .filter { if (favoritesOnly) it.isFavorited else true }
            .filter { if (query.isBlank()) true else it.title.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadNotes()
    }

    /** Loads notes from the repository, updating loading and error states. */
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

    /** Toggles the favorite status of a note by its [noteId]. */
    fun toggleFavorite(noteId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(noteId)
                .onSuccess { updated ->
                    _notes.value = _notes.value.map { if (it.id == updated.id) updated else it }
                }
                .onFailure { _error.value = it.message ?: "Failed to update note" }
        }
    }

    /** Toggles the favorites-only filter on/off. */
    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
