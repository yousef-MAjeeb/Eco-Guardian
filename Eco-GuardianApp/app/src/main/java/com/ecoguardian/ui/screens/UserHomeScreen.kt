package com.ecoguardian.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.ecoguardian.data.FinishedBg
import com.ecoguardian.data.FinishedText
import com.ecoguardian.data.LightGrayBg
import com.ecoguardian.data.PendingBg
import com.ecoguardian.data.PendingText
import com.ecoguardian.data.PrimaryGreen
import com.ecoguardian.data.dummyReports
import com.ecoguardian.data.Report

@Composable
fun UserHomeScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // 1. حالة جديدة عشان نتحكم في إظهار أو إخفاء نافذة الإضافة
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredReports = dummyReports.filter { report ->
        if (selectedTabIndex == 0) report.status == "Pending" else report.status == "Finished"
    }

    // 2. استخدام Scaffold عشان نقدر نضيف الزرار العائم (FAB)
    Scaffold(
        modifier = Modifier.systemBarsPadding(), // لحل مشكلة شريط الإشعارات
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, // لما نضغط، نعرض النافذة
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGrayBg)
                .padding(innerPadding) // مساحة للـ Scaffold
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredReports) { report ->
                    ReportCard(report = report)
                }
            }
        }
    }

    // 3. لو المتغير بقى true، هنعرض الـ Dialog اللي فيه كود المعرض
    if (showAddDialog) {
        AddReportDialog(onDismiss = { showAddDialog = false })
    }
}

// 4. النافذة المنبثقة لاختيار الصورة

@Composable
fun AddReportDialog(onDismiss: () -> Unit) {
    // حالات لحفظ الصورة والنصوص
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var reportTitle by remember { mutableStateOf("") }
    var reportDescription by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "إضافة بلاغ جديد", fontWeight = FontWeight.Bold) },
        text = {
            // ضفنا verticalScroll عشان لو الكيبورد فتحت المستخدم يقدر ينزل ويطلع
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. جزء الصورة
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp) // صغرتها شوية عشان تدي مساحة للنصوص
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لم يتم اختيار صورة", color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("اختر صورة للمشكلة")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. حقل إدخال العنوان
                OutlinedTextField(
                    value = reportTitle,
                    onValueChange = { reportTitle = it },
                    label = { Text("عنوان البلاغ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true, // سطر واحد بس
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. حقل إدخال التفاصيل
                OutlinedTextField(
                    value = reportDescription,
                    onValueChange = { reportDescription = it },
                    label = { Text("تفاصيل المشكلة والمكان") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3, // بيبدأ بـ 3 سطور عشان يدي مساحة للكتابة
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // هنا هتكتب اللوجيك اللي بياخد (selectedImageUri و reportTitle و reportDescription) ويبعتهم للسيرفر
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                // الزرار مش هيشتغل غير لو المستخدم كتب عنوان واختار صورة (اختياري بس بيحسن الـ UX)
                enabled = reportTitle.isNotBlank() && selectedImageUri != null
            ) {
                Text("حفظ البلاغ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Red)
            }
        }
    )
}

// باقي الكود بتاعك (CustomTabRow و ReportCard) زي ما هو بالظبط من غير تعديل

@Composable
fun CustomTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Pending Reports", "Finished Reports")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) PrimaryGreen else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ReportCard(report: Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = report.imageUrl,
                contentDescription = "Report Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val badgeBgColor = if (report.status == "Pending") PendingBg else FinishedBg
            val badgeTextColor = if (report.status == "Pending") PendingText else FinishedText

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeBgColor,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Text(
                    text = report.status,
                    color = badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}