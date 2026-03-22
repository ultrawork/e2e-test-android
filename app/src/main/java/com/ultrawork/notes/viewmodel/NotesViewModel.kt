package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor() : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(
        notes,
        _searchQuery,
        _showFavoritesOnly
    ) { notesList, query, favoritesOnly ->
        var result = notesList
        if (favoritesOnly) {
            result = result.filter { it.isFavorited }
        }
        if (query.isNotBlank()) {
            result = result.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.update { !it }
    }

    fun toggleFavorite(noteId: Long) {
        _notes.update { notes ->
            notes.map { note ->
                if (note.id == noteId) note.copy(isFavorited = !note.isFavorited)
                else note
            }
        }
    }

    fun loadNotes() {
        // TODO: Load notes from repository
        // For now, add some sample data
        _notes.value = listOf(
            Note(
                id = 1,
                title = "Shopping List",
                content = "Milk, Eggs, Bread",
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            ),
            Note(
                id = 2,
                title = "Meeting Notes",
                content = "Discuss project timeline",
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            ),
            Note(
                id = 3,
                title = "Ideas",
                content = "New app features",
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            ),
            Note(
                id = 4,
                title = "Travel Plans",
                content = "Book flights and hotel",
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            ),
            Note(
                id = 5,
                title = "Work Tasks",
                content = "Complete documentation",
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            )
        )
    }
}
