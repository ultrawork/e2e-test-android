package com.ultrawork.notes.data.repository.fake

import com.ultrawork.notes.data.Category
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import java.util.Date

/**
 * In-memory реализация [NotesRepository] для работы без бэкенда.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableMapOf<Long, Note>()
    private var nextId = 1L

    init {
        val sampleCategory = Category(id = "sample-cat-1", name = "Work", color = "#FF5733")
        val note1 = Note(
            id = nextId++,
            title = "First Note",
            content = "Content of the first note",
            createdAt = Date(),
            updatedAt = Date(),
            categories = listOf(sampleCategory)
        )
        val note2 = Note(
            id = nextId++,
            title = "Second Note",
            content = "Content of the second note",
            createdAt = Date(),
            updatedAt = Date(),
            categories = emptyList()
        )
        notes[note1.id] = note1
        notes[note2.id] = note2
    }

    override suspend fun getNotes(categoryId: String?): Result<List<Note>> {
        val result = if (categoryId != null) {
            notes.values.filter { note ->
                note.categories.any { it.id == categoryId }
            }
        } else {
            notes.values.toList()
        }
        return Result.success(result)
    }

    override suspend fun getNote(id: Long): Result<Note> {
        val note = notes[id]
            ?: return Result.failure(NoSuchElementException("Note not found: $id"))
        return Result.success(note)
    }

    override suspend fun create(note: Note): Result<Note> {
        val newNote = note.copy(id = nextId++)
        notes[newNote.id] = newNote
        return Result.success(newNote)
    }

    override suspend fun update(note: Note): Result<Note> {
        if (!notes.containsKey(note.id)) {
            return Result.failure(NoSuchElementException("Note not found: ${note.id}"))
        }
        notes[note.id] = note
        return Result.success(note)
    }

    override suspend fun delete(id: Long): Result<Unit> {
        if (!notes.containsKey(id)) {
            return Result.failure(NoSuchElementException("Note not found: $id"))
        }
        notes.remove(id)
        return Result.success(Unit)
    }
}
