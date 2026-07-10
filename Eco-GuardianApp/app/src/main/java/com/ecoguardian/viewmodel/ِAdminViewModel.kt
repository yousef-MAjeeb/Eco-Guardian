package com.ecoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoguardian.data.Report
import com.ecoguardian.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // 1. هنضيف متغير showLoading ونخليه true كقيمة افتراضية
    fun fetchAllReports(showLoading: Boolean = true) {
        viewModelScope.launch {
            // هنعرض حالة التحميل بس لو المتغير ده true
            if (showLoading) {
                _uiState.value = AdminUiState.Loading
            }
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

    // 2. جوه الـ Realtime Subscription، هننادي عليها ونقوله ماتعرضش Loading
    private fun setupRealtimeSubscription() {
        viewModelScope.launch {
            try {
                val channel = SupabaseClient.client.channel("public-reports")
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "reports"
                }
                channel.subscribe()
                changes.collect {
                    // أول ما يحصل تغيير، حدث القائمة في الخلفية بدون ما تبوظ شكل الشاشة
                    fetchAllReports(showLoading = false)
                }
            } catch (e: Exception) {
                println("Realtime Error: ${e.message}")
            }
        }
    }


    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    // وعدل دوال الحذف والتحديث عشان تبعت رسالة نجاح أو فشل
    fun markAsFinished(reportId: String) {
        viewModelScope.launch {
            try {
                db.update({ set("status", "finished") }) { filter { eq("id", reportId) } }
                _snackbarEvent.emit("تم تحديث حالة التقرير بنجاح")
                fetchAllReports(showLoading = false)
            } catch (e: Exception) {
                _snackbarEvent.emit("فشل تحديث التقرير: ${e.message}")
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            try {
                db.delete { filter { eq("id", reportId) } }
                _snackbarEvent.emit("تم حذف التقرير بنجاح")
                fetchAllReports(showLoading = false)
            } catch (e: Exception) {
                _snackbarEvent.emit("فشل حذف التقرير: ${e.message}")
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