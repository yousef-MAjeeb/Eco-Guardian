package com.ecoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.ecoguardian.data.Report
import com.ecoguardian.viewmodel.UserReportsViewModel

@Composable
fun UserHomeScreen(viewModel: UserReportsViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val pendingReports by viewModel.pendingReports.collectAsState()
    val finishedReports by viewModel.finishedReports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val displayedReports = if (selectedTabIndex == 0) pendingReports else finishedReports

    // ضفنا Scaffold هنا عشان نحط زرار الإضافة العائم
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // هنا المفروض هنحط كود الانتقال لشاشة إضافة بلاغ لما تخلصوها
                },
                containerColor = Color(0xFF2E6B4F), // PrimaryGreen
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة بلاغ جديد"
                )
            }
        },
        containerColor = Color(0xFFF9FAFB) // LightGrayBg
    ) { paddingValues ->

        // المحتوى بتاعنا زي ما هو بس أخدنا الـ paddingValues من الـ Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E6B4F))
                }
            } else if (displayedReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "لا توجد بلاغات في هذه القائمة", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // عشان الزرار العايم مايغطيش على آخر كارت
                ) {
                    items(displayedReports) { report ->
                        ReportCard(report = report)
                    }
                }
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
                model = report.photoUrl,
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
                    text = report.reportText.ifEmpty { "بلاغ بدون تفاصيل" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (report.locationLink.isNotEmpty()) {
                    Text(
                        text = "📍 الرابط: ${report.locationLink}",
                        fontSize = 12.sp,
                        color = Color.Blue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val isPending = report.status.lowercase() == "pending"
            val badgeBgColor = if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            val badgeTextColor = if (isPending) Color(0xFFE68A00) else Color(0xFF4CAF50)

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeBgColor,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Text(
                    text = report.status.uppercase(),
                    color = badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// دالة CustomTabRow تفضل موجودة هنا تحت الكارت زي ما هي
@Composable
fun CustomTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Pending", "Finished")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = Color(0xFF2E6B4F) // PrimaryGreen
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (selectedTabIndex == index) Color(0xFF2E6B4F) else Color.Gray
                    )
                }
            )
        }
    }
}