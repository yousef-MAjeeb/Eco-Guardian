package com.ecoguardian.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ecoguardian.BuildConfig
import com.ecoguardian.viewmodel.ReportViewModel
import io.github.jan.supabase.SupabaseClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReportScreen(
    imageUri: Uri,
    supabase: SupabaseClient,
    userId: String,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ReportViewModel = viewModel(
        factory = ReportViewModel.Factory(supabase, BuildConfig.GEMINI_API_KEY)
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(imageUri) {
        viewModel.analyzeImage(imageUri, context)
    }

    LaunchedEffect(state.isDone) {
        if (state.isDone) {
            onSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Report Generation") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Waste Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "AI Generated Report",
                style = MaterialTheme.typography.titleLarge
            )

            if (state.isAnalyzing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analyzing image...")
                }
            } else {
                OutlinedTextField(
                    value = state.reportText,
                    onValueChange = { viewModel.onReportTextChange(it) },
                    label = { Text("Report Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6
                )
            }

            OutlinedTextField(
                value = state.locationLink,
                onValueChange = { viewModel.onLocationChange(it) },
                label = { Text("Paste location link") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.submitReport(userId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isAnalyzing && !state.isSubmitting && state.reportText.isNotBlank()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Report")
                }
            }
        }
    }
}
