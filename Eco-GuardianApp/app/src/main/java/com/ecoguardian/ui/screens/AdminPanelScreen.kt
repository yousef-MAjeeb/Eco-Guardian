
package com.ecoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ecoguardian.data.* // استيراد الألوان وملف Report
import com.ecoguardian.viewmodel.AdminUiState
import com.ecoguardian.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: AdminViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isPendingTabSelected by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = LightGrayBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text("Review community reports", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrayBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // شريط التبويبات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                TextButton(
                    onClick = { isPendingTabSelected = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isPendingTabSelected) PrimaryGreen else Color.Transparent,
                        contentColor = if (isPendingTabSelected) Color.White else PrimaryGreen
                    )
                ) { Text("Pending Reports") }

                TextButton(
                    onClick = { isPendingTabSelected = false },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (!isPendingTabSelected) PrimaryGreen else Color.Transparent,
                        contentColor = if (!isPendingTabSelected) Color.White else PrimaryGreen
                    )
                ) { Text("Finished Reports") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // معالجة الحالات
            when (uiState) {
                is AdminUiState.Loading -> {
                    // عرض مؤشر التحميل أثناء انتظار البيانات من Supabase
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is AdminUiState.Error -> {
                    val errorMessage = (uiState as AdminUiState.Error).message
                    LaunchedEffect(errorMessage) {
                        snackbarHostState.showSnackbar(
                            message = errorMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = { viewModel.fetchAllReports() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
                is AdminUiState.Success -> {
                    val successState = uiState as AdminUiState.Success
                    RenderReportsList(
                        isPending = isPendingTabSelected,
                        pendingList = successState.pendingReports,
                        finishedList = successState.finishedReports,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// دالة مساعدة لتنظيف كود الـ UI وتقليل التكرار
@Composable
fun RenderReportsList(
    isPending: Boolean,
    pendingList: List<Report>,
    finishedList: List<Report>,
    viewModel: AdminViewModel
) {
    if (isPending) {
        Text("Pending Reports", fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pendingList) { report ->
                PendingReportCard(
                    report = report,
                    onMarkFinished = { viewModel.markAsFinished(report.id) },
                    onDelete = { viewModel.deleteReport(report.id) }
                )
            }
        }
    } else {
        Text("Finished Reports", fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(finishedList) { report ->
                FinishedReportCard(report = report)
            }
        }
    }
}

@Composable
fun PendingReportCard(report: Report, onMarkFinished: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = report.photoUrl,
                        contentDescription = "Trash Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(report.reportText.substring(0,40), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.reportText, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }

                // شارة الحالة
                Text(
                    text = report.status,
                    fontSize = 10.sp,
                    color = PendingText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(PendingBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onMarkFinished,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Mark Finished", fontSize = 12.sp)
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FinishedReportCard(report: Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = report.photoUrl,
                contentDescription = "Trash Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(report.reportText.substring(0,50), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(report.reportText, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // شارة النهاية
            Text(
                text = report.status,
                fontSize = 10.sp,
                color = FinishedText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(FinishedBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}



//package com.ecoguardian.ui.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import coil.compose.AsyncImage
//import com.ecoguardian.data.* // استيراد الألوان وملف Report
//import com.ecoguardian.viewmodel.AdminUiState
//import com.ecoguardian.viewmodel.AdminViewModel
////@Preview
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminPanelScreen(viewModel: AdminViewModel = viewModel()) {
//    val uiState by viewModel.uiState.collectAsState()
//    var isPendingTabSelected by remember { mutableStateOf(true) }
//    val snackbarHostState = remember { SnackbarHostState() }
//    Scaffold(
//        containerColor = LightGrayBg,
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
//        topBar = {
//            TopAppBar(
//                title = {
//                    Column {
//                        Text("Admin Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
//                        Text("Review community reports", fontSize = 12.sp, color = Color.Gray)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrayBg)
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .padding(horizontal = 16.dp)
//                .fillMaxSize()
//        ) {
//            // شريط التبويبات
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp)
//                    .clip(RoundedCornerShape(24.dp))
//                    .background(Color.White)
//                    .padding(4.dp)
//            ) {
//                TextButton(
//                    onClick = { isPendingTabSelected = true },
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxHeight(),
//                    colors = ButtonDefaults.textButtonColors(
//                        containerColor = if (isPendingTabSelected) PrimaryGreen else Color.Transparent,
//                        contentColor = if (isPendingTabSelected) Color.White else PrimaryGreen
//                    )
//                ) { Text("Pending Reports") }
//
//                TextButton(
//                    onClick = { isPendingTabSelected = false },
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxHeight(),
//                    colors = ButtonDefaults.textButtonColors(
//                        containerColor = if (!isPendingTabSelected) PrimaryGreen else Color.Transparent,
//                        contentColor = if (!isPendingTabSelected) Color.White else PrimaryGreen
//                    )
//                ) { Text("Finished Reports") }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // معالجة الحالات
//            when (uiState) {
//                is AdminUiState.Loading -> {
//                    // مؤقتاً لعرض الـ Dummy Data لو Supabase لسه مش جاهز
//                    // يمكنك استبدال dummyReports بـ successState.pendingReports لاحقاً
//                    RenderReportsList(
////                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
////                            CircularProgressIndicator(color = PrimaryGreen)
//                        isPending = isPendingTabSelected,
//                        pendingList = dummyReports.filter { it.status == "Pending" },
//                        finishedList = dummyReports.filter { it.status == "Finished" },
//                        viewModel = viewModel
//                    )
//                }
//                is AdminUiState.Error -> {
//                    val errorMessage = (uiState as AdminUiState.Error).message
//                    LaunchedEffect(errorMessage) {
//                        snackbarHostState.showSnackbar(
//                            message = errorMessage,
//                            duration = SnackbarDuration.Short)
//
//                    }
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Button(onClick = { viewModel.fetchAllReports() }) {
//                            Text("إعادة المحاولة")
//                        }
//                    }
//                }
//                is AdminUiState.Success -> {
//                    val successState = uiState as AdminUiState.Success
//                    RenderReportsList(
//                        isPending = isPendingTabSelected,
//                        pendingList = successState.pendingReports,
//                        finishedList = successState.finishedReports,
//                        viewModel = viewModel
//                    )
//                }
//            }
//        }
//    }
//}
//
//// دالة مساعدة لتنظيف كود الـ UI وتقليل التكرار
//@Composable
//fun RenderReportsList(
//    isPending: Boolean,
//    pendingList: List<Report>,
//    finishedList: List<Report>,
//    viewModel: AdminViewModel
//) {
//    if (isPending) {
//        Text("Pending Reports", fontWeight = FontWeight.Bold, color = PrimaryGreen)
//        Spacer(modifier = Modifier.height(8.dp))
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//            items(pendingList) { report ->
//                PendingReportCard(
//                    report = report,
//                    onMarkFinished = { viewModel.markAsFinished(report.id) },
//                    onDelete = { viewModel.deleteReport(report.id)
//                    }
//                )
//            //                items(pendingList) { report ->
////                PendingReportCard(
////                    report = report,
////                    onMarkFinished = { viewModel.markAsFinished(report.id) },
////                    onDelete = { viewModel.deleteReport(report.id) }
////                )
//            }
//        }
//    } else {
//        Text("Finished Reports", fontWeight = FontWeight.Bold, color = PrimaryGreen)
//        Spacer(modifier = Modifier.height(8.dp))
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//            items(finishedList) { report ->
//                FinishedReportCard(report = report)
//            }
//        }
//    }
//}
//
//@Composable
//fun PendingReportCard(report: Report, onMarkFinished: () -> Unit, onDelete: () -> Unit) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Row(modifier = Modifier.weight(1f)) {
//                    AsyncImage(
//                        model = report.imageUrl,
//                        contentDescription = "Trash Image",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .size(60.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(Color.LightGray)
//                    )
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Column {
//                        Text(report.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(report.description, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
//                    }
//                }
//
//                // شارة الحالة باستخدام الألوان الجديدة
//                Text(
//                    text = report.status,
//                    fontSize = 10.sp,
//                    color = PendingText,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .background(PendingBg, RoundedCornerShape(8.dp))
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                Button(onClick = onMarkFinished, modifier = Modifier
//                    .weight(1f)
//                    .height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), shape = RoundedCornerShape(10.dp)) {
//                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
//                    Text(" Mark Finished", fontSize = 12.sp)
//                }
//                Button(onClick = onDelete, modifier = Modifier
//                    .weight(1f)
//                    .height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(10.dp)) {
//                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
//                    Text(" Delete", fontSize = 12.sp)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun FinishedReportCard(report: Report) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Row(modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//            AsyncImage(
//                model = report.imageUrl,
//                contentDescription = "Trash Image",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(60.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color.LightGray)
//            )
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(report.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(report.description, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // شارة النهاية
//            Text(
//                text = report.status,
//                fontSize = 10.sp,
//                color = FinishedText,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier
//                    .background(FinishedBg, RoundedCornerShape(8.dp))
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//        }
//    }
//}