package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.data.remote.ApiException
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest
import com.ultrawork.notes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    data object Loading : UiState()
    data class Success(val notes: List<NoteDto>) : UiState()
    data class Error(val message: String, val code: Int) : UiState()
}

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredNotes: StateFlow<List<NoteDto>> = combine(_uiState, _searchQuery) { state, query ->
        val notes = (state as? UiState.Success)?.notes ?: emptyList()
        if (query.isBlank()) {
            notes
        } else {
            notes.filter { it.title.contains(query, ignoreCase = true) }
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
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getNotes()
                .onSuccess { notes ->
                    _uiState.value = UiState.Success(notes)
                }
                .onFailure { error ->
                    _uiState.value = mapError(error)
                }
        }
    }

    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            repository.createNote(NoteRequest(title, content))
                .onSuccess { loadNotes() }
                .onFailure { error ->
                    _uiState.value = mapError(error)
                }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
                .onSuccess { loadNotes() }
                .onFailure { error ->
                    _uiState.value = mapError(error)
                }
        }
    }

    private fun mapError(error: Throwable): UiState.Error {
        if (error is ApiException) {
            val message = when (error.code) {
                401 -> "Unauthorized (401)"
                403 -> "Forbidden (403)"
                in 500..599 -> "Server error (${error.code})"
                else -> "${error.message} (${error.code})"
            }
            return UiState.Error(message, error.code)
        }
        return UiState.Error(error.message ?: "Unknown error", 0)
    }
}
