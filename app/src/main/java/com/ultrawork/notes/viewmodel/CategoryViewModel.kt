package com.ultrawork.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel for managing category list state and CRUD actions. */
@HiltViewModel
class CategoryViewModel @Inject constructor() : ViewModel() {

    data class CategoryUi(
        val id: Long,
        val name: String,
        val colorHex: String
    )

    data class UiState(
        val isLoading: Boolean = false,
        val categories: List<CategoryUi> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var nextCategoryId = 4L

    /** Loads categories into UI state. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = listOf(
                        CategoryUi(id = 1L, name = "Work", colorHex = "#FF5722"),
                        CategoryUi(id = 2L, name = "Personal", colorHex = "#4CAF50"),
                        CategoryUi(id = 3L, name = "Ideas", colorHex = "#03A9F4")
                    ),
                    error = null
                )
            }
        }
    }

    /** Creates a category when validation passes. */
    fun create(name: String, colorHex: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(error = "Category name must not be empty") }
            return
        }
        if (!isValidHexColor(colorHex)) {
            _uiState.update { it.copy(error = "Category color must be a valid hex") }
            return
        }
        _uiState.update {
            it.copy(
                categories = it.categories + CategoryUi(
                    id = nextCategoryId++,
                    name = trimmedName,
                    colorHex = colorHex
                ),
                error = null
            )
        }
    }

    /** Updates an existing category when validation passes. */
    fun update(id: Long, name: String, colorHex: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(error = "Category name must not be empty") }
            return
        }
        if (!isValidHexColor(colorHex)) {
            _uiState.update { it.copy(error = "Category color must be a valid hex") }
            return
        }
        if (_uiState.value.categories.none { it.id == id }) {
            _uiState.update { it.copy(error = "Category not found") }
            return
        }
        _uiState.update {
            it.copy(
                categories = it.categories.map { category ->
                    if (category.id == id) {
                        category.copy(name = trimmedName, colorHex = colorHex)
                    } else {
                        category
                    }
                },
                error = null
            )
        }
    }

    /** Deletes a category by id. */
    fun delete(id: Long) {
        if (_uiState.value.categories.none { it.id == id }) {
            _uiState.update { it.copy(error = "Category not found") }
            return
        }
        _uiState.update {
            it.copy(
                categories = it.categories.filterNot { category -> category.id == id },
                error = null
            )
        }
    }

    private fun isValidHexColor(colorHex: String): Boolean {
        return Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$").matches(colorHex)
    }
}
