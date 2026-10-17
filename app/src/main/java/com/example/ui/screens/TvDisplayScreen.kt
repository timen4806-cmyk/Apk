package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.components.DigitalClockView
import com.example.ui.components.PrayerCard
import com.example.ui.components.RunningTextMarquee
import com.example.ui.theme.GoldAccent

@Composable
fun TvDisplayScreen(
    viewModel: MainViewModel,
    onExitTvMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onExitTvMode() }

    val currentTime by viewModel.currentTime.collectAsState()
    val dateMasehi by viewModel.currentDateMasehi.collectAsState()
    val dateHijri by viewModel.currentDateHijri.collectAsState()
    val prayerSchedule by viewModel.prayerSchedule.collectAsState()
    val nextPrayerName by viewModel.nextPrayerName.collectAsState()
    val nextPrayerCountdown by viewModel.nextPrayerCountdown.collectAsState()
    val isIqomahCountingDown by viewModel.isIqomahCountingDown.collectAsState()
    val iqomahPrayerName by viewModel.iqomahPrayerName.collectAsState()
    val iqomahCountdownText by viewModel.iqomahCountdownText.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val dailyOfficers by viewModel.dailyOfficers.collectAsState()

    // Find current or next officer
    val activeOfficer = dailyOfficers.find { it.prayerName.equals(nextPrayerName, ignoreCase = true) }
        ?: dailyOfficers.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("tv_display_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isCompactLandscape = screenHeight < 520.dp
        val isVeryWide = screenWidth > 900.dp

        // High-definition Mosque Background
        Image(
            painter = painterResource(id = R.drawable.img_masjid_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Luxury Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xDD05140D),
                            Color(0xF506160E)
                        )
                    )
                )
        )

        // Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (isCompactLandscape) 8.dp else 16.dp,
                    start = if (isCompactLandscape) 12.dp else 20.dp,
                    end = if (isCompactLandscape) 12.dp else 20.dp,
                    bottom = 6.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TV Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactLandscape) 40.dp else 52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F3A2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_mosque),
                            contentDescription = null,
                            modifier = Modifier
                                .size(if (isCompactLandscape) 36.dp else 46.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(if (isCompactLandscape) 8.dp else 14.dp))

                    Column {
                        Text(
                            text = "MASJID JAMI' AT-TAQWA",
                            color = GoldAccent,
                            fontSize = if (isCompactLandscape) 16.sp else 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Penancangan, Cipocok Jaya - Kota Serang, Banten",
                            color = Color(0xFFCBD5E1),
                            fontSize = if (isCompactLandscape) 10.sp else 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Exit TV Mode Button
                IconButton(
                    onClick = onExitTvMode,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Keluar Mode TV",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactLandscape) 4.dp else 10.dp))

            // Center Area: Digital Clock & Active Officer Widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLandscape) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock & Countdown
                Box(modifier = Modifier.weight(if (isVeryWide) 1.6f else 1.3f)) {
                    DigitalClockView(
                        currentTime = currentTime,
                        dateMasehi = dateMasehi,
                        dateHijri = dateHijri,
                        nextPrayerName = nextPrayerName,
                        nextPrayerCountdown = nextPrayerCountdown,
                        isIqomahCountingDown = isIqomahCountingDown,
                        iqomahPrayerName = iqomahPrayerName,
                        iqomahCountdownText = iqomahCountdownText,
                        cityName = prayerSchedule.cityName,
                        isTvDisplay = !isCompactLandscape
                    )
                }

                // Officer On Duty (Imam, Muadzin, Bilal for current/upcoming prayer)
                activeOfficer?.let { officer ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(if (isCompactLandscape) 14.dp else 20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC091E16)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFF10B981))),
                            width = 1.5.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isCompactLandscape) 10.dp else 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F5A47))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PETUGAS SHALAT ${officer.prayerName.uppercase()}",
                                    color = GoldAccent,
                                    fontSize = if (isCompactLandscape) 9.sp else 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isCompactLandscape) 6.dp else 10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(if (isCompactLandscape) 16.dp else 18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "IMAM:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = if (isCompactLandscape) 9.sp else 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = officer.imam,
                                        color = Color.White,
                                        fontSize = if (isCompactLandscape) 12.sp else 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactLandscape) 4.dp else 8.dp))

                            Column {
                                Text(
                                    text = "MUADZIN: ${officer.muadzin}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = if (isCompactLandscape) 10.sp else 12.sp
                                )
                                Text(
                                    text = "BILAL: ${officer.bilal}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = if (isCompactLandscape) 10.sp else 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isCompactLandscape) 4.dp else 6.dp))

                            Text(
                                text = "● Status: Siap Bertugas",
                                color = Color(0xFF34D399),
                                fontSize = if (isCompactLandscape) 9.sp else 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactLandscape) 4.dp else 10.dp))

            // Lower Area: Horizontal 6 Prayer Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLandscape) 6.dp else 10.dp)
            ) {
                val prayers = listOf(
                    Triple("Subuh", prayerSchedule.subuh, "الصبح"),
                    Triple("Terbit", prayerSchedule.terbit, "الشروq"),
                    Triple("Dzuhur", prayerSchedule.dzuhur, "الظهر"),
                    Triple("Ashar", prayerSchedule.ashar, "العصر"),
                    Triple("Maghrib", prayerSchedule.maghrib, "المغرب"),
                    Triple("Isya", prayerSchedule.isya, "العشاء")
                )

                prayers.forEach { (name, time, arabic) ->
                    PrayerCard(
                        name = name,
                        time = time,
                        arabicName = arabic,
                        isNextPrayer = name.equals(nextPrayerName, ignoreCase = true),
                        modifier = Modifier.weight(1f),
                        isTvDisplay = !isCompactLandscape
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactLandscape) 4.dp else 10.dp))

            // Bottom Area: Continuous Running Text Marquee
            RunningTextMarquee(
                announcements = announcements,
                isTvDisplay = !isCompactLandscape
            )
        }
    }
}
