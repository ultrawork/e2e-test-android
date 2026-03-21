package com.ultrawork.notes.data.repository.impl

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note

/**
 * Реализация [NotesRepository] через Retrofit [ApiService].
 */
class NotesRepositoryImpl(private val api: ApiService) : NotesRepository {

    override suspend fun getNotes(categoryId: String?): Result<List<Note>> = runCatching {
        api.getNotes(categoryId)
    }

    override suspend fun getNote(id: Long): Result<Note> = runCatching {
        api.getNote(id)
    }

    override suspend fun create(note: Note): Result<Note> = runCatching {
        api.createNote(note)
    }

    override suspend fun update(note: Note): Result<Note> = runCatching {
        api.updateNote(note.id, note)
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        val response = api.deleteNote(id)
        if (!response.isSuccessful) {
            throw RuntimeException("Delete failed with code ${response.code()}")
        }
    }
}
