package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.DailyPrayerOfficer
import com.example.data.model.EidSchedule
import com.example.data.model.FridaySchedule
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NightSurface

@Composable
fun OfficersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dailyOfficers by viewModel.dailyOfficers.collectAsState()
    val fridaySchedule by viewModel.fridaySchedule.collectAsState()
    val eidSchedules by viewModel.eidSchedules.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("5 Waktu Harian", "Shalat Jum'at", "Hari Raya Idul Fitri & Adha")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("officers_screen")
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color(0xFF092017),
            contentColor = GoldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = GoldAccent
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedSubTab) {
                0 -> {
                    // Shalat 5 Waktu Harian
                    item {
                        Text(
                            text = "Jadwal Petugas Shalat 5 Waktu",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Imam, Muadzin & Bilal Masjid Jami' At-Taqwa Penancangan",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    items(dailyOfficers) { officer ->
                        DailyOfficerDetailedCard(officer = officer)
                    }
                }

                1 -> {
                    // Shalat Jum'at
                    item {
                        FridayScheduleCard(schedule = fridaySchedule)
                    }
                }

                2 -> {
                    // Hari Raya
                    item {
                        Text(
                            text = "Jadwal Shalat Hari Raya",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tahun 1448 H / Masjid Jami' At-Taqwa Penancangan",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    items(eidSchedules) { eid ->
                        EidScheduleCard(schedule = eid)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyOfficerDetailedCard(officer: DailyPrayerOfficer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF1E4535), Color(0xFF135841))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F5A47))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Waktu " + officer.prayerName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3310B981))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = officer.notes,
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Imam
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF071911))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "IMAM UTAMA", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = officer.imam, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Muadzin & Bilal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Muadzin
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF071911))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "MUADZIN", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = officer.muadzin, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Bilal
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF071911))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "BILAL", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = officer.bilal, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun FridayScheduleCard(schedule: FridaySchedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(GoldAccent, Color(0xFF10B981))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "JUM'AT BERKAH",
                        color = Color(0xFF071911),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = schedule.time,
                    color = Color(0xFF6EE7B7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Judul Khutbah:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Text(
                text = "“${schedule.khutbahTitle}”",
                color = GoldAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Khatib & Imam
            FridayPersonRow(label = "KHATIB JUM'AT", name = schedule.khatib, icon = Icons.Default.MenuBook)
            Spacer(modifier = Modifier.height(8.dp))
            FridayPersonRow(label = "IMAM SHALAT JUM'AT", name = schedule.imam, icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(8.dp))
            FridayPersonRow(label = "MUADZIN", name = schedule.muadzin, icon = Icons.Default.RecordVoiceOver)
            Spacer(modifier = Modifier.height(8.dp))
            FridayPersonRow(label = "BILAL JUM'AT (TARQIE)", name = schedule.bilal, icon = Icons.Default.VolumeUp)
        }
    }
}

@Composable
fun FridayPersonRow(label: String, name: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF071911))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EidScheduleCard(schedule: EidSchedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF135841), GoldAccent)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.eidType,
                    color = GoldAccent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F5A47))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = schedule.time,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tanggal: ${schedule.date}",
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Lokasi: ${schedule.location}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            FridayPersonRow(label = "KHATIB ID", name = schedule.khatib, icon = Icons.Default.MenuBook)
            Spacer(modifier = Modifier.height(6.dp))
            FridayPersonRow(label = "IMAM ID", name = schedule.imam, icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(6.dp))
            FridayPersonRow(label = "BILAL / TAKBIRAN", name = schedule.bilal, icon = Icons.Default.Campaign)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Catatan: ${schedule.notes}",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
