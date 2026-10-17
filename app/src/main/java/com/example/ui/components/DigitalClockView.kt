package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NightSurface

@Composable
fun DigitalClockView(
    currentTime: String,
    dateMasehi: String,
    dateHijri: String,
    nextPrayerName: String,
    nextPrayerCountdown: String,
    isIqomahCountingDown: Boolean,
    iqomahPrayerName: String,
    iqomahCountdownText: String,
    cityName: String = "Penancangan, Serang (Banten)",
    isTvDisplay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clockCardBg = if (isTvDisplay) Color(0xCC091E16) else NightSurface

    val iqomahPulseColor by animateColorAsState(
        targetValue = if (isIqomahCountingDown) Color(0xFFEF4444) else Color(0xFF0F5A47),
        label = "iqomah_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_clock_card"),
        shape = RoundedCornerShape(if (isTvDisplay) 20.dp else 16.dp),
        colors = CardDefaults.cardColors(containerColor = clockCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    GoldAccent.copy(alpha = 0.5f),
                    Color(0xFF10B981).copy(alpha = 0.4f),
                    GoldAccent.copy(alpha = 0.5f)
                )
            ),
            width = if (isTvDisplay) 2.dp else 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTvDisplay) 24.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location & Standards Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(if (isTvDisplay) 18.dp else 14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$cityName • Standar Kemenag RI",
                    color = Color(0xFFCBD5E1),
                    fontSize = if (isTvDisplay) 14.sp else 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Digital Time (HH:mm:ss)
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = if (isTvDisplay) 64.sp else 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("digital_clock_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dual Dates (Masehi & Hijriyah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateMasehi,
                    color = Color(0xFFE2E8F0),
                    fontSize = if (isTvDisplay) 18.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "  •  ",
                    color = GoldAccent,
                    fontSize = if (isTvDisplay) 18.sp else 13.sp
                )
                Text(
                    text = dateHijri,
                    color = GoldAccent,
                    fontSize = if (isTvDisplay) 18.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown Status Banner
            if (isIqomahCountingDown) {
                // Iqomah Alert Active Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(iqomahPulseColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isTvDisplay) 24.dp else 18.dp)
                        )
                        Text(
                            text = "HITUNG MUNDUR IQOMAH $iqomahPrayerName: $iqomahCountdownText",
                            color = Color.White,
                            fontSize = if (isTvDisplay) 18.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Next Prayer Countdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F3A2C))
                        .border(1.dp, Color(0xFF1D5A46), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(if (isTvDisplay) 20.dp else 16.dp)
                        )
                        Text(
                            text = "Menuju Shalat $nextPrayerName : $nextPrayerCountdown",
                            color = GoldAccent,
                            fontSize = if (isTvDisplay) 17.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
