package com.ecoguardian.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// الكلاس الجديد اللي بيطابق جدول reports في Supabase
@Serializable
data class Report(
    val id: String="" ,
    @SerialName("user_id") val userId: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("location_link") val locationLink: String? = null,
    @SerialName("report_text") val reportText: String = "",
    val status: String = "", // 'pending' أو 'finished'
    @SerialName("created_at") val createdAt: String = ""
)