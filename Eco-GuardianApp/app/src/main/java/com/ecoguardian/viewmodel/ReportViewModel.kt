package com.ecoguardian.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ecoguardian.data.GeminiService
import com.ecoguardian.data.ReportRepository
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class ReportUiState(
    val isAnalyzing: Boolean = false,
    val reportText: String = "",
    val locationLink: String = "",
    val isSubmitting: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null
)

class ReportViewModel(
    private val repository: ReportRepository,
    private val geminiService: GeminiService,
    private val apiKey: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var currentImageBytes: ByteArray? = null

    fun analyzeImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val bytes = outputStream.toByteArray()
                currentImageBytes = bytes

                val report = geminiService.generateReport(bytes, apiKey)
                _uiState.update { it.copy(isAnalyzing = false, reportText = report) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, error = "Failed to analyze image: ${e.localizedMessage}") }
            }
        }
    }

    fun onReportTextChange(text: String) {
        _uiState.update { it.copy(reportText = text) }
    }

    fun onLocationChange(link: String) {
        _uiState.update { it.copy(locationLink = link) }
    }

    fun submitReport(userId: String) {
        val bytes = currentImageBytes ?: return
        val state = _uiState.value
        if (state.reportText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                repository.submitReport(
                    userId = userId,
                    imageBytes = bytes,
                    reportText = state.reportText,
                    locationLink = state.locationLink
                )
                _uiState.update { it.copy(isSubmitting = false, isDone = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = "Submission failed: ${e.localizedMessage}") }
            }
        }
    }

    class Factory(
        private val supabase: SupabaseClient,
        private val apiKey: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                val repository = ReportRepository(supabase)
                val geminiService = GeminiService(HttpClient(Android))
                @Suppress("UNCHECKED_CAST")
                return ReportViewModel(repository, geminiService, apiKey) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
