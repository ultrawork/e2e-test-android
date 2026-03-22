package com.ultrawork.notes.data.repository.impl

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import javax.inject.Inject

/**
 * Real implementation of [NotesRepository] backed by [ApiService].
 */
class NotesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotesRepository {

    override suspend fun getNotes(favoritesOnly: Boolean): List<Note> {
        return apiService.getNotes(if (favoritesOnly) true else null)
    }

    override suspend fun toggleFavorite(id: String): Note {
        return apiService.toggleFavorite(id)
    }
}
