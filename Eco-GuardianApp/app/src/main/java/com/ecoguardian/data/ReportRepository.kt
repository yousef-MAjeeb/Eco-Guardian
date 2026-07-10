package com.ecoguardian.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class ReportRepository {
    private val supabase = SupabaseClient.client

    suspend fun submitReport(
        userId: String,
        imageBytes: ByteArray,
        reportText: String,
        locationLink: String
    ) {
        val fileName = "$userId/${UUID.randomUUID()}.jpg"
        val bucket = supabase.storage.from("report-photos")

        bucket.upload(fileName, imageBytes)

        val photoUrl = bucket.publicUrl(fileName)

        val report = Report(
            userId = userId,
            photoUrl = photoUrl,
            reportText = reportText,
            locationLink = locationLink,
            status = "pending"
        )
        supabase.from("reports").insert(report)
    }
}