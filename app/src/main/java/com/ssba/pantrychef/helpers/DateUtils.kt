package com.ssba.pantrychef.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    /**
     * Converts a millisecond timestamp into a formatted date string.
     * @param timestamp The time in milliseconds since the epoch.
     * @return A formatted string like "15 October 2025", or "N/A" if the timestamp is 0.
     */
    fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) {
            return "N/A"
        }
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}