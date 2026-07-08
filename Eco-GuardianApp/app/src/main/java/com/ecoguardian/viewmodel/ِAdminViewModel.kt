package com.ecoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoguardian.data.Report
import com.ecoguardian.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(val pendingReports: List<Report>, val finishedReports: List<Report>) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val db = SupabaseClient.client.postgrest["reports"]

    init {
        // أول ما الشاشة تفتح، نجلب البيانات ونشغل الـ Realtime
        fetchAllReports()
        setupRealtimeSubscription()
    }

    fun fetchAllReports() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val allReports = db.select().decodeList<Report>()

                val pending = allReports.filter { it.status == "pending" }
                val finished = allReports.filter { it.status == "finished" }

                _uiState.value = AdminUiState.Success(pending, finished)
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error fetching data: ${e.message}")
            }
        }
    }

    // إعداد الاتصال اللحظي (Realtime)
    private fun setupRealtimeSubscription() {
        viewModelScope.launch {
            try {
                // 1. إنشاء قناة اتصال
                val channel = SupabaseClient.client.channel("public-reports")

                // 2. تحديد الجدول اللي هنراقبه
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "reports"
                }

                // 3. تفعيل الاشتراك
                channel.subscribe()

                // 4. الاستماع لأي تغيير (Insert, Update, Delete)
                changes.collect { action ->
                    // أول ما يحصل تغيير، حدث القائمة فوراً
                    fetchAllReports()
                }
            } catch (e: Exception) {
                println("Realtime Error: ${e.message}")
            }
        }
    }

    fun markAsFinished(reportId: Int) {
        viewModelScope.launch {
            try {
                db.update({ set("status", "finished") }) { filter { eq("id", reportId) } }
                // شلنا fetchAllReports من هنا لأن الـ Realtime هيحدثها تلقائياً
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Failed to update report")
            }
        }
    }

    fun deleteReport(reportId: Int) {
        viewModelScope.launch {
            try {
                db.delete { filter { eq("id", reportId) } }
                // شلنا fetchAllReports من هنا لأن الـ Realtime هيحدثها تلقائياً
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Failed to delete report")
            }
        }
    }
}
//ملاحظة هامة جداً عشان الكود ده يشتغل:
//الـ Realtime بيكون مقفول افتراضياً في Supabase لأسباب تخص الأمان وتقليل استهلاك السيرفر. عشان الكود اللي فوق ده يحس بالتغييرات، لازم تفعل الخاصية دي من لوحة تحكم موقع Supabase:
//
//افتح مشروعك على موقع Supabase.
//
//من القائمة الجانبية اختار Database وبعدين Replication.
//
//هتلاقي خيار اسمه Source وجنبه جداول مشروعك.
//
//فعل الزرار (Toggle) اللي جنب جدول reports.