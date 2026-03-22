package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

/**
 * Repository interface for notes operations.
 */
interface NotesRepository {
    suspend fun getNotes(favoritesOnly: Boolean = false): List<Note>
    suspend fun toggleFavorite(id: String): Note
}
