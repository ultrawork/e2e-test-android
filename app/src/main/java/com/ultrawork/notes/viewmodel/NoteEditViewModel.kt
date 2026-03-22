package com.ultrawork.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel for creating and editing notes. */
@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class CategoryUi(
        val id: Long,
        val name: String,
        val colorHex: String
    )

    data class UiState(
        val isEditing: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val title: String = "",
        val content: String = "",
        val categories: List<CategoryUi> = emptyList(),
        val selectedCategoryIds: Set<Long> = emptySet(),
        val error: String? = null
    )

    sealed class SaveResult {
        data class Created(val noteId: Long) : SaveResult()
        data class Updated(val noteId: Long) : SaveResult()
        data class ValidationError(val message: String) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    private val noteId: Long? = when (val raw = savedStateHandle.get<Any?>("noteId")) {
        is Long -> raw
        is Int -> raw.toLong()
        is String -> raw.toLongOrNull()
        else -> null
    }

    private val _uiState = MutableStateFlow(UiState(isEditing = noteId != null))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _saveResults = MutableSharedFlow<SaveResult>(extraBufferCapacity = 1)
    val saveResults: SharedFlow<SaveResult> = _saveResults.asSharedFlow()

    private var nextNoteId = 1000L

    init {
        loadInitialData()
    }

    /** Updates title in UI state. */
    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, error = null) }
    }

    /** Updates content in UI state. */
    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent, error = null) }
    }

    /** Toggles a category selection. */
    fun onToggleCategory(categoryId: Long) {
        _uiState.update {
            val updatedSelection = it.selectedCategoryIds.toMutableSet().apply {
                if (contains(categoryId)) remove(categoryId) else add(categoryId)
            }
            it.copy(selectedCategoryIds = updatedSelection, error = null)
        }
    }

    /** Saves the note and emits one-off result events. */
    fun save() {
        val validationError = validateTitle()
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            _saveResults.tryEmit(SaveResult.ValidationError(validationError))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            if (noteId != null) {
                _saveResults.emit(SaveResult.Updated(noteId))
            } else {
                _saveResults.emit(SaveResult.Created(nextNoteId++))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val categories = listOf(
            CategoryUi(id = 1L, name = "Work", colorHex = "#FF5722"),
            CategoryUi(id = 2L, name = "Personal", colorHex = "#4CAF50"),
            CategoryUi(id = 3L, name = "Ideas", colorHex = "#03A9F4")
        )
        if (noteId != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    title = "Sample note",
                    content = "Sample content",
                    selectedCategoryIds = setOf(1L, 3L),
                    error = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    error = null
                )
            }
        }
    }

    private fun validateTitle(): String? {
        return if (_uiState.value.title.trim().isEmpty()) {
            "Note title must not be empty"
        } else {
            null
        }
    }
}
