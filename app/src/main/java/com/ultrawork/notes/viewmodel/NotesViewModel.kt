package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrawork.notes.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor() : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredNotes = combine(_notes, _searchQuery) { notesList, query ->
        if (query.isBlank()) {
            notesList
        } else {
            notesList.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Инициализируем тестовыми данными
        viewModelScope.launch {
            _notes.update {
                listOf(
                    Note(id = "1", title = "Покупки", content = "Купить молоко, хлеб, яйца"),
                    Note(id = "2", title = "Работа", content = "Завершить проект до пятницы"),
                    Note(id = "3", title = "Спорт", content = "Тренировка в 18:00"),
                    Note(id = "4", title = "Книги", content = "Прочитать новую книгу по Kotlin"),
                    Note(id = "5", title = "Встреча", content = "Встреча с командой в 15:00"),
                    Note(id = "6", title = "Идеи", content = "Записать идеи для нового приложения"),
                    Note(id = "7", title = "Рецепты", content = "Попробовать новый рецепт пасты"),
                    Note(id = "8", title = "Путешествия", content = "Планирование поездки на море"),
                    Note(id = "9", title = "Обучение", content = "Изучить Compose Navigation"),
                    Note(id = "10", title = "Финансы", content = "Составить бюджет на месяц")
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}