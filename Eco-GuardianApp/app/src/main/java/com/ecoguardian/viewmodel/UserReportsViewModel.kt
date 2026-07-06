package com.ecoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoguardian.data.Report
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserReportsViewModel(private val supabase: SupabaseClient) : ViewModel() {

    private val _pendingReports = MutableStateFlow<List<Report>>(emptyList())
    val pendingReports: StateFlow<List<Report>> = _pendingReports.asStateFlow()

    private val _finishedReports = MutableStateFlow<List<Report>>(emptyList())
    val finishedReports: StateFlow<List<Report>> = _finishedReports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserReports(showLoading = true)
        setupRealtime() // تفعيل المراقبة الفورية أول ما الشاشة تفتح
    }

    private fun fetchUserReports(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) _isLoading.value = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id

                if (userId != null) {
                    val pending = supabase.postgrest["reports"].select {
                        filter {
                            eq("user_id", userId)
                            eq("status", "pending")
                        }
                    }.decodeList<Report>()
                    _pendingReports.value = pending

                    val finished = supabase.postgrest["reports"].select {
                        filter {
                            eq("user_id", userId)
                            eq("status", "finished")
                        }
                    }.decodeList<Report>()
                    _finishedReports.value = finished
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // إنشاء قناة اتصال مع قاعدة البيانات
                val channel = supabase.channel("user_reports_changes")

                // مراقبة جدول reports بالكامل بدون فلتر هنا
                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "reports"
                }

                // لما يحصل تغيير، الدالة دي هتشتغل وتجيب بيانات المستخدم ده بس
                changeFlow.onEach {
                    fetchUserReports(showLoading = false)
                }.launchIn(viewModelScope)

                // تفعيل الاشتراك في القناة
                channel.subscribe()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}