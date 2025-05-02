package com.example.Locify.utility

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DateTimeUtils @Inject constructor() {

    /**
     * Format date to string using pattern
     * @param date Date to format
     * @param pattern Format pattern
     * @return Formatted date string
     */
    fun formatDate(date: Date, pattern: String = "MMM dd, yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Format time to string using pattern
     * @param date Date containing time to format
     * @param pattern Format pattern
     * @return Formatted time string
     */
    fun formatTime(date: Date, pattern: String = "h:mm a"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Format date and time to string using pattern
     * @param date Date to format
     * @param pattern Format pattern
     * @return Formatted date and time string
     */
    fun formatDateTime(date: Date, pattern: String = "MMM dd, yyyy h:mm a"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Get date with time set to beginning of day (00:00:00)
     * @param date Input date
     * @return Date set to beginning of day
     */
    fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Get date with time set to end of day (23:59:59)
     * @param date Input date
     * @return Date set to end of day
     */
    fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * Calculate time difference in minutes between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return Difference in minutes
     */
    fun getTimeDifferenceMinutes(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    }

    /**
     * Get human-readable relative time string
     * @param date Date to format
     * @return Relative time string (e.g., "2 hours ago", "in 3 days")
     */
    fun getRelativeTimeString(date: Date): String {
        val now = Date()
        val diffInMillis = date.time - now.time
        val isInFuture = diffInMillis > 0

        val absDiffInMillis = Math.abs(diffInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(absDiffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(absDiffInMillis)

        return when {
            days > 0 -> {
                val prefix = if (isInFuture) "in " else ""
                val suffix = if (isInFuture) "" else " ago"
                "$prefix$days day${if (days > 1) "s" else ""}$suffix"
            }
            hours > 0 -> {
                val prefix = if (isInFuture) "in " else ""
                val suffix = if (isInFuture) "" else " ago"
                "$prefix$hours hour${if (hours > 1) "s" else ""}$suffix"
            }
            minutes > 0 -> {
                val prefix = if (isInFuture) "in " else ""
                val suffix = if (isInFuture) "" else " ago"
                "$prefix$minutes minute${if (minutes > 1) "s" else ""}$suffix"
            }
            else -> if (isInFuture) "just now" else "just now"
        }
    }

    /**
     * Get next occurrence of day of week
     * @param dayOfWeek Day of week (Calendar.MONDAY, Calendar.TUESDAY, etc.)
     * @return Date of next occurrence
     */
    fun getNextDayOfWeek(dayOfWeek: Int): Date {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate days to add
        val daysToAdd = if (currentDayOfWeek <= dayOfWeek) {
            dayOfWeek - currentDayOfWeek
        } else {
            7 - (currentDayOfWeek - dayOfWeek)
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar.time
    }

    /**
     * Add days to date
     * @param date Input date
     * @param days Number of days to add
     * @return Date with days added
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    /**
     * Check if a date is today
     * @param date Date to check
     * @return True if date is today
     */
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = date

        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Check if a date is tomorrow
     * @param date Date to check
     * @return True if date is tomorrow
     */
    fun isTomorrow(date: Date): Boolean {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)

        val calendar = Calendar.getInstance()
        calendar.time = date

        return tomorrow.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Format date as "Today", "Tomorrow", or date string
     * @param date Date to format
     * @return Formatted string
     */
    fun getSmartDateString(date: Date): String {
        return when {
            isToday(date) -> "Today"
            isTomorrow(date) -> "Tomorrow"
            else -> formatDate(date)
        }
    }
}