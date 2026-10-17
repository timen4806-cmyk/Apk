package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.ui.theme.GoldAccent

@Composable
fun RunningTextMarquee(
    announcements: List<Announcement>,
    modifier: Modifier = Modifier,
    isTvDisplay: Boolean = false
) {
    val activeList = announcements.filter { it.isActive }
    val fullText = if (activeList.isEmpty()) {
        "Selamat datang di Masjid Jami' At-Taqwa Penancangan. Menjaga kekhusyukan dan kenyamanan ibadah bersama."
    } else {
        activeList.joinToString(separator = "   ۞   ") { it.text }
    }

    val bannerHeight = if (isTvDisplay) 52.dp else 42.dp
    val fontSize = if (isTvDisplay) 20.sp else 14.sp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .testTag("running_text_marquee"),
        color = Color(0xFF071911),
        shape = RoundedCornerShape(if (isTvDisplay) 0.dp else 12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0F5A47), Color(0xFF10B981))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Pengumuman",
                        tint = GoldAccent,
                        modifier = Modifier.size(if (isTvDisplay) 20.dp else 16.dp)
                    )
                    Text(
                        text = "INFO MASJID",
                        color = Color.White,
                        fontSize = if (isTvDisplay) 14.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Marquee Scrolling Text
            Text(
                text = fullText,
                color = GoldAccent,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = if (isTvDisplay) 40.dp else 30.dp
                    )
            )
        }
    }
}
