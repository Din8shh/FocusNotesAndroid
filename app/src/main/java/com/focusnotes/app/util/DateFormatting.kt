package com.focusnotes.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Mirrors the iOS `DateFormatting` utility. */
object DateFormatting {

    private val dueDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

    fun formatDuration(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val secs = safe % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
    }

    fun formatDueDate(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(dueDateFormatter)
    }

    /** End of the current day (23:59:59.999), as epoch millis. */
    fun endOfTodayMillis(): Long {
        val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .minusNanos(1_000_000)
        return endOfDay.toInstant().toEpochMilli()
    }

    fun isOverdue(epochMillis: Long): Boolean {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.isBefore(LocalDate.now())
    }
}
