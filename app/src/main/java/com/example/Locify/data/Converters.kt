package com.example.Locify.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.Locify.data.RepeatInterval
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Type converters for Room database to handle complex data types
 */
class Converters {
    private val gson = Gson()

    // LocalDateTime converters
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    // List<Int> converters (for repeat custom days)
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // RepeatInterval converters
    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval {
        return try {
            RepeatInterval.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatInterval.NEVER
        }
    }
}