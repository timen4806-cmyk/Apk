package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent

@Composable
fun PrayerCard(
    name: String,
    time: String,
    arabicName: String,
    isNextPrayer: Boolean,
    modifier: Modifier = Modifier,
    isTvDisplay: Boolean = false
) {
    val prayerIcon: ImageVector = when (name.lowercase()) {
        "imsak" -> Icons.Default.NightsStay
        "subuh" -> Icons.Default.WbTwilight
        "terbit" -> Icons.Default.WbSunny
        "dhuha" -> Icons.Default.LightMode
        "dzuhur" -> Icons.Default.WbSunny
        "ashar" -> Icons.Default.BrightnessMedium
        "maghrib" -> Icons.Default.Brightness4
        "isya" -> Icons.Default.Bedtime
        else -> Icons.Default.AccessTime
    }

    val cardBackground = if (isNextPrayer) {
        Brush.verticalGradient(
            listOf(Color(0xFF135841), Color(0xFF0B3326))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xCC0D261C), Color(0xCC081812))
        )
    }

    val borderColor = if (isNextPrayer) GoldAccent else Color(0xFF1E4535)

    Box(
        modifier = modifier
            .testTag("prayer_card_${name.lowercase()}")
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .border(
                width = if (isNextPrayer) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(if (isTvDisplay) 14.dp else 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon & Next Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = prayerIcon,
                    contentDescription = name,
                    tint = if (isNextPrayer) GoldAccent else Color(0xFF6EE7B7),
                    modifier = Modifier.size(if (isTvDisplay) 22.dp else 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Prayer Name
            Text(
                text = name.uppercase(),
                color = if (isNextPrayer) GoldAccent else Color(0xFFE2E8F0),
                fontSize = if (isTvDisplay) 16.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Arabic Name
            Text(
                text = arabicName,
                color = if (isNextPrayer) Color(0xFFFDE68A) else Color(0xFF94A3B8),
                fontSize = if (isTvDisplay) 13.sp else 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Prayer Time
            Text(
                text = time,
                color = Color.White,
                fontSize = if (isTvDisplay) 24.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (isNextPrayer) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BERIKUTNYA",
                        color = Color(0xFF092419),
                        fontSize = if (isTvDisplay) 10.sp else 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
