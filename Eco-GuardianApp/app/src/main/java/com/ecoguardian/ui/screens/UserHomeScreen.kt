package com.ecoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // فلترة البيانات بناءً على التبويب المحدد
    val filteredReports = dummyReports.filter { report ->
        if (selectedTabIndex == 0) report.status == "Pending" else report.status == "Finished"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
            .systemBarsPadding() // 👈 هذا هو التعديل الذي سيمنع تداخل التصميم مع شريط الإشعارات
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // مكون الـ Tabs المخصص
        CustomTabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // قائمة التقارير
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // مساحة سفلية
        ) {
            items(filteredReports) { report ->
                ReportCard(report = report)
            }
        }
    }
}

@Composable
fun CustomTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Pending Reports", "Finished Reports")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50)) // شكل الكبسولة
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
            // صورة التقرير باستخدام Coil
            AsyncImage(
                model = report.imageUrl,
                contentDescription = "Report Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray) // لون مبدئي أثناء التحميل
            )

            Spacer(modifier = Modifier.width(12.dp))

            // النصوص (العنوان والتفاصيل)
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                    maxLines = 2, // يظهر نقط إذا زاد النص
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // شارة الحالة (Status Badge)
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