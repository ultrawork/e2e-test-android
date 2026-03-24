package com.ultrawork.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ultrawork.notes.model.Note

/**
 * Room database definition for notes storage.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(DateConverters::class)
abstract class NotesDatabase : RoomDatabase()
