package com.example.ritik_2.modules

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ComplaintWithId(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val urgency: String,
    val status: String,
    val timestamp: Long,
    val contactInfo: String,
    val hasAttachment: Boolean
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return format.format(date)
    }
}