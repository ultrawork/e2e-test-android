package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.model.Note
import com.ultrawork.notes.repository.NotesRepository
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

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(
        _notes, _searchQuery, _showFavoritesOnly
    ) { notesList, query, favoritesOnly ->
        var result = notesList
        if (favoritesOnly) {
            result = result.filter { it.isFavorited }
        }
        if (query.isNotBlank()) {
            result = result.filter { it.title.contains(query, ignoreCase = true) }
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

    /**
     * Загружает заметки из репозитория.
     */
    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getNotes()
                .onSuccess { _notes.value = it }
                .onFailure { _error.value = it.message ?: "Ошибка загрузки заметок" }
            _isLoading.value = false
        }
    }

    /**
     * Создаёт новую заметку.
     */
    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.createNote(title, content)
                .onSuccess { loadNotes() }
                .onFailure { _error.value = it.message ?: "Ошибка создания заметки" }
            _isLoading.value = false
        }
    }

    /**
     * Удаляет заметку по id.
     */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.deleteNote(id)
                .onSuccess { loadNotes() }
                .onFailure { _error.value = it.message ?: "Ошибка удаления заметки" }
            _isLoading.value = false
        }
    }

    /**
     * Переключает избранное для заметки.
     */
    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            _error.value = null
            repository.toggleFavorite(id)
                .onSuccess { updatedNote ->
                    _notes.value = _notes.value.map {
                        if (it.id == id) updatedNote else it
                    }
                }
                .onFailure { _error.value = it.message ?: "Ошибка переключения избранного" }
        }
    }

    /**
     * Переключает фильтр «только избранные».
     */
    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
}
