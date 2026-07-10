package com.ecoguardian.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// الألوان بتاعتك زي ما هي عشان التصميم يفضل سليم
val PrimaryGreen = Color(0xFF2E6B4F)
val PendingBg = Color(0xFFFFF3E0)
val PendingText = Color(0xFFE68A00)
val FinishedBg = Color(0xFFE8F5E9)
val FinishedText = Color(0xFF4CAF50)
val LightGrayBg = Color(0xFFF9FAFB)

// الكلاس الجديد اللي بيطابق جدول reports في Supabase
@Serializable
data class Report(
    val id: String="" ,
    @SerialName("user_id") val userId: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("location_link") val locationLink: String = "",
    @SerialName("report_text") val reportText: String = "",
    val status: String = "", // 'pending' أو 'finished'
    @SerialName("created_at") val createdAt: String = ""
)