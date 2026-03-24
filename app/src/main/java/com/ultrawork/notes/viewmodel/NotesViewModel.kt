package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor() : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun loadNotes() {
        // TODO: Load notes from repository
        // For now, add some sample data
        _notes.value = listOf(
            Note(id = 1, title = "Shopping List", content = "Milk, Eggs, Bread", createdAt = Date(), updatedAt = Date()),
            Note(id = 2, title = "Meeting Notes", content = "Discuss project timeline", createdAt = Date(), updatedAt = Date()),
            Note(id = 3, title = "Ideas", content = "New app features", createdAt = Date(), updatedAt = Date()),
            Note(id = 4, title = "Travel Plans", content = "Book flights and hotel", createdAt = Date(), updatedAt = Date()),
            Note(id = 5, title = "Work Tasks", content = "Complete documentation", createdAt = Date(), updatedAt = Date())
        )
    }
}
