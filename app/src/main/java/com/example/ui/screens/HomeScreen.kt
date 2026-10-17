package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.data.model.DailyPrayerOfficer
import com.example.ui.components.DigitalClockView
import com.example.ui.components.PrayerCard
import com.example.ui.components.RunningTextMarquee
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NightSurface

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    currentTime: String,
    dateMasehi: String,
    dateHijri: String,
    nextPrayerName: String,
    nextPrayerCountdown: String,
    isIqomahCountingDown: Boolean,
    iqomahPrayerName: String,
    iqomahCountdownText: String,
    prayerSchedule: com.example.data.model.PrayerDaySchedule,
    dailyOfficers: List<DailyPrayerOfficer>,
    announcements: List<com.example.data.model.Announcement>,
    onNavigateToTab: (Int) -> Unit,
    onEnterTvMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Mosque Banner with Atmosphere
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWide) 130.dp else 160.dp)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_masjid_bg),
                        contentDescription = "Masjid Jami' At-Taqwa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x77071911),
                                        Color(0xF0071911)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Jadwal & Informasi Jamaah",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Masjid Jami' At-Taqwa",
                            color = Color.White,
                            fontSize = if (isWide) 20.sp else 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Penancangan - Cipocok Jaya, Kota Serang",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Digital Clock & Next Prayer Countdown Card (shown on mobile or when not in wide split mode)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DigitalClockView(
                        currentTime = currentTime,
                        dateMasehi = dateMasehi,
                        dateHijri = dateHijri,
                        nextPrayerName = nextPrayerName,
                        nextPrayerCountdown = nextPrayerCountdown,
                        isIqomahCountingDown = isIqomahCountingDown,
                        iqomahPrayerName = iqomahPrayerName,
                        iqomahCountdownText = iqomahCountdownText,
                        cityName = prayerSchedule.cityName
                    )
                }
            }

            // Prayer Times (Kemenag RI Standard)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jadwal Shalat Hari Ini",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standar Kemenag RI",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val prayerList = listOf(
                        Triple("Imsak", prayerSchedule.imsak, "الإمساك"),
                        Triple("Subuh", prayerSchedule.subuh, "الصبح"),
                        Triple("Terbit", prayerSchedule.terbit, "الشروق"),
                        Triple("Dhuha", prayerSchedule.dhuha, "الضحى"),
                        Triple("Dzuhur", prayerSchedule.dzuhur, "الظهر"),
                        Triple("Ashar", prayerSchedule.ashar, "العصر"),
                        Triple("Maghrib", prayerSchedule.maghrib, "المغرب"),
                        Triple("Isya", prayerSchedule.isya, "العشاء")
                    )

                    if (isWide) {
                        // Wide landscape: Responsive 2 rows of 4 cards
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                prayerList.take(4).forEach { (name, time, arabic) ->
                                    PrayerCard(
                                        name = name,
                                        time = time,
                                        arabicName = arabic,
                                        isNextPrayer = name.equals(nextPrayerName, ignoreCase = true),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                prayerList.drop(4).forEach { (name, time, arabic) ->
                                    PrayerCard(
                                        name = name,
                                        time = time,
                                        arabicName = arabic,
                                        isNextPrayer = name.equals(nextPrayerName, ignoreCase = true),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Mobile portrait: Smooth horizontal scrolling row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(prayerList) { (name, time, arabic) ->
                                PrayerCard(
                                    name = name,
                                    time = time,
                                    arabicName = arabic,
                                    isNextPrayer = name.equals(nextPrayerName, ignoreCase = true),
                                    modifier = Modifier.width(96.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Feature Shortcuts
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NightSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF105B43)))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            QuickActionItem(
                                icon = Icons.Default.People,
                                label = "Petugas",
                                onClick = { onNavigateToTab(1) }
                            )
                            QuickActionItem(
                                icon = Icons.Default.Event,
                                label = "Agenda",
                                onClick = { onNavigateToTab(2) }
                            )
                            QuickActionItem(
                                icon = Icons.Default.VolunteerActivism,
                                label = "Donasi",
                                onClick = { onNavigateToTab(3) }
                            )
                            QuickActionItem(
                                icon = Icons.Default.Tv,
                                label = "Layar TV",
                                onClick = onEnterTvMode
                            )
                            QuickActionItem(
                                icon = Icons.Default.AdminPanelSettings,
                                label = "Admin",
                                onClick = { onNavigateToTab(4) }
                            )
                        }
                    }
                }
            }

            // Petugas Shalat Hari Ini
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Petugas Shalat Berjamaah Hari Ini",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lihat Semua",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToTab(1) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isWide) {
                        // 2-column grid for wide screens
                        val chunked = dailyOfficers.chunked(2)
                        chunked.forEach { rowOfficers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOfficers.forEach { officer ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        OfficerItemCard(officer = officer)
                                    }
                                }
                                if (rowOfficers.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        dailyOfficers.forEach { officer ->
                            OfficerItemCard(officer = officer)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F3A2B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OfficerItemCard(officer: DailyPrayerOfficer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF163729)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Prayer Name Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F5A47))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = officer.prayerName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Officer details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Imam: ", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = officer.imam, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Muadzin: ", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text(text = officer.muadzin, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                    Text(text = "  •  Bilal: ", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text(text = officer.bilal, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                }
            }

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x3310B981))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Siap",
                    color = Color(0xFF34D399),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
