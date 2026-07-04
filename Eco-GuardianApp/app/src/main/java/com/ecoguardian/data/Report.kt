package com.ecoguardian.data

import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF2E6B4F)
val PendingBg = Color(0xFFFFF3E0)
val PendingText = Color(0xFFE68A00)
val FinishedBg = Color(0xFFE8F5E9)
val FinishedText = Color(0xFF4CAF50)
val LightGrayBg = Color(0xFFF9FAFB)


data class Report(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: String // "Pending" or "Finished"
)


val dummyReports = listOf(
    Report("1", "Plastic bags beside creek trail", "Several bags and loose bottles were found near the walking path after the weekend.", "https://picsum.photos/200", "Pending"),
    Report("2", "Food wrappers near bus stop", "Overflowing bin and scattered packaging close to the station seating area.", "https://picsum.photos/201", "Pending"),
    Report("3", "Recycling spill at corner lot", "Cardboard and cans cleaned up by the municipal response crew this morning.", "https://picsum.photos/202", "Finished"),
    Report("4", "Park entrance bottle pile", "Report resolved after cleanup team removed glass bottles from the main gate.", "https://picsum.photos/203", "Finished")
)