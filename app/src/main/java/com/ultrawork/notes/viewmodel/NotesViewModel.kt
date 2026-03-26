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
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

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

    /**
     * Loads notes from the remote API.
     */
    fun loadNotes() {
        viewModelScope.launch {
            try {
                _errorState.value = null
                _notes.value = repository.getNotes()
            } catch (e: HttpException) {
                _errorState.value = "Ошибка: ${e.code()}"
            } catch (e: Exception) {
                _errorState.value = "Ошибка: ${e.message}"
            }
        }
    }

    /**
     * Creates a new note via the remote API.
     */
    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                _errorState.value = null
                val note = repository.createNote(title, content)
                _notes.value = _notes.value + note
            } catch (e: HttpException) {
                _errorState.value = "Ошибка: ${e.code()}"
            } catch (e: Exception) {
                _errorState.value = "Ошибка: ${e.message}"
            }
        }
    }

    /**
     * Deletes a note by ID via the remote API.
     */
    fun deleteNote(id: Long) {
        viewModelScope.launch {
            try {
                _errorState.value = null
                repository.deleteNote(id)
                _notes.value = _notes.value.filter { it.id != id }
            } catch (e: HttpException) {
                _errorState.value = "Ошибка: ${e.code()}"
            } catch (e: Exception) {
                _errorState.value = "Ошибка: ${e.message}"
            }
        }
    }
}
