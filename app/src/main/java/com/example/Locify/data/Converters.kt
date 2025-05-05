package com.example.Locify.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Type converters for Room database to handle complex data types
 */
class Converters {
    private val gson = Gson()

    // LocalDateTime converters
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    // List<Int> converters for repeat days
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    // RepeatType enum converters
    @TypeConverter
    fun fromRepeatType(value: RepeatType): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatType(value: String): RepeatType {
        return try {
            RepeatType.valueOf(value)
        } catch (e: Exception) {
            RepeatType.NONE
        }
    }
}