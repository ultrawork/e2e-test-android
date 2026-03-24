package com.ultrawork.notes.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room type converters for persisting [Date] values.
 */
class DateConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let(::Date)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
