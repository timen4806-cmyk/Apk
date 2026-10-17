package com.example

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DigitalClockView
import com.example.ui.components.MosqueHeader
import com.example.ui.components.RunningTextMarquee
import com.example.ui.screens.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MosqueAppTheme
import com.example.ui.theme.NightSurface

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Keep screen awake for mosque display / signage
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val isTvMode by mainViewModel.isTvMode.collectAsState()
            val isLandscapeForced by mainViewModel.isLandscapeForced.collectAsState()

            // Dynamic orientation controller
            LaunchedEffect(isLandscapeForced) {
                requestedOrientation = if (isLandscapeForced) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            // Request Notification Permission on Android 13+
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { /* granted or denied */ }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MosqueAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isTvMode) {
                        TvDisplayScreen(
                            viewModel = mainViewModel,
                            onExitTvMode = { mainViewModel.setTvMode(false) }
                        )
                    } else {
                        ResponsiveAppContent(viewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ResponsiveAppContent(viewModel: MainViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight || maxWidth >= 600.dp
        val isWideScreen = maxWidth >= 840.dp

        if (isLandscape) {
            LandscapeResponsiveLayout(
                viewModel = viewModel,
                isWideScreen = isWideScreen
            )
        } else {
            PortraitMobileLayout(viewModel = viewModel)
        }
    }
}

@Composable
fun LandscapeResponsiveLayout(
    viewModel: MainViewModel,
    isWideScreen: Boolean
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isLandscapeForced by viewModel.isLandscapeForced.collectAsState()
    val isCloudSynced by viewModel.isCloudSynced.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    val currentTime by viewModel.currentTime.collectAsState()
    val dateMasehi by viewModel.currentDateMasehi.collectAsState()
    val dateHijri by viewModel.currentDateHijri.collectAsState()
    val prayerSchedule by viewModel.prayerSchedule.collectAsState()
    val nextPrayerName by viewModel.nextPrayerName.collectAsState()
    val nextPrayerCountdown by viewModel.nextPrayerCountdown.collectAsState()
    val isIqomahCountingDown by viewModel.isIqomahCountingDown.collectAsState()
    val iqomahPrayerName by viewModel.iqomahPrayerName.collectAsState()
    val iqomahCountdownText by viewModel.iqomahCountdownText.collectAsState()
    val dailyOfficers by viewModel.dailyOfficers.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    val navItems = listOf(
        Triple("Beranda", Icons.Default.Home, 0),
        Triple("Petugas", Icons.Default.People, 1),
        Triple("Agenda", Icons.Default.Event, 2),
        Triple("Kas & Donasi", Icons.Default.AccountBalanceWallet, 3),
        Triple("Admin", Icons.Default.AdminPanelSettings, 4)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // NavigationRail for landscape on TV, PC, Tablet, & Smartphone
            NavigationRail(
                containerColor = Color(0xFF071911),
                contentColor = GoldAccent,
                header = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F3A2B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_mosque),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .testTag("landscape_nav_rail")
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        navItems.forEach { (label, icon, tabIndex) ->
                            val isSelected = selectedTab == tabIndex
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tabIndex) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) GoldAccent else Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GoldAccent else Color(0xFF94A3B8)
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = GoldAccent,
                                    selectedTextColor = GoldAccent,
                                    indicatorColor = Color(0xFF0F5A47),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("rail_item_$tabIndex")
                            )
                        }
                    }

                    // Bottom Action Shortcuts in Rail
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.setTvMode(true) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF135841))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Mode TV",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleForceLandscape() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isLandscapeForced) Color(0xFF10B981) else Color(0xFF13382B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotasi",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setDarkMode(!isDarkMode) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF13382B))
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Tema",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            VerticalDivider(color = Color(0xFF1E4535), thickness = 1.dp)

            // Main Content Area
            if (isWideScreen) {
                // Dual-Pane Layout for TV, PC, Laptop, and Tablets
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Left Persistent Sidebar Widget (~320dp)
                    Column(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF091E16))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mosque Banner Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = "JAMI' AT-TAQWA",
                                    color = GoldAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Penancangan, Kota Serang",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Digital Clock & Countdown
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

                        // Active Officer Snapshot
                        val currentOfficer = dailyOfficers.find { it.prayerName.equals(nextPrayerName, ignoreCase = true) }
                            ?: dailyOfficers.firstOrNull()

                        currentOfficer?.let { officer ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = NightSurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841))))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "PETUGAS ${officer.prayerName.uppercase()}",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Imam: ${officer.imam}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Muadzin: ${officer.muadzin}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Cloud Sync Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF071911))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCloudSynced) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCloudSynced) "Cloud: $lastSyncTime" else "Menyinkronkan...",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    VerticalDivider(color = Color(0xFF1E4535), thickness = 1.dp)

                    // Right Content Pane (Selected Tab Screen)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        TabContentRouter(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            currentTime = currentTime,
                            dateMasehi = dateMasehi,
                            dateHijri = dateHijri,
                            nextPrayerName = nextPrayerName,
                            nextPrayerCountdown = nextPrayerCountdown,
                            isIqomahCountingDown = isIqomahCountingDown,
                            iqomahPrayerName = iqomahPrayerName,
                            iqomahCountdownText = iqomahCountdownText,
                            prayerSchedule = prayerSchedule,
                            dailyOfficers = dailyOfficers,
                            announcements = announcements
                        )
                    }
                }
            } else {
                // Single Pane for smaller landscape (Phone Landscape / Foldable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TabContentRouter(
                        selectedTab = selectedTab,
                        viewModel = viewModel,
                        currentTime = currentTime,
                        dateMasehi = dateMasehi,
                        dateHijri = dateHijri,
                        nextPrayerName = nextPrayerName,
                        nextPrayerCountdown = nextPrayerCountdown,
                        isIqomahCountingDown = isIqomahCountingDown,
                        iqomahPrayerName = iqomahPrayerName,
                        iqomahCountdownText = iqomahCountdownText,
                        prayerSchedule = prayerSchedule,
                        dailyOfficers = dailyOfficers,
                        announcements = announcements
                    )
                }
            }
        }

        // Sticky Footer Marquee Across Entire Landscape Width
        RunningTextMarquee(
            announcements = announcements,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PortraitMobileLayout(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isLandscapeForced by viewModel.isLandscapeForced.collectAsState()
    val isCloudSynced by viewModel.isCloudSynced.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    val currentTime by viewModel.currentTime.collectAsState()
    val dateMasehi by viewModel.currentDateMasehi.collectAsState()
    val dateHijri by viewModel.currentDateHijri.collectAsState()
    val prayerSchedule by viewModel.prayerSchedule.collectAsState()
    val nextPrayerName by viewModel.nextPrayerName.collectAsState()
    val nextPrayerCountdown by viewModel.nextPrayerCountdown.collectAsState()
    val isIqomahCountingDown by viewModel.isIqomahCountingDown.collectAsState()
    val iqomahPrayerName by viewModel.iqomahPrayerName.collectAsState()
    val iqomahCountdownText by viewModel.iqomahCountdownText.collectAsState()
    val dailyOfficers by viewModel.dailyOfficers.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MosqueHeader(
                isCloudSynced = isCloudSynced,
                lastSyncTime = lastSyncTime,
                isDarkMode = isDarkMode,
                isLandscapeForced = isLandscapeForced,
                onToggleDarkMode = { viewModel.setDarkMode(!isDarkMode) },
                onToggleOrientation = { viewModel.toggleForceLandscape() },
                onEnterTvMode = { viewModel.setTvMode(true) },
                onNotificationClick = { viewModel.triggerPrayerReminderNow() }
            )
        },
        bottomBar = {
            Column {
                // Running text marquee above navigation bar
                RunningTextMarquee(announcements = announcements)

                NavigationBar(
                    containerColor = Color(0xFF092017),
                    contentColor = GoldAccent,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple("Beranda", Icons.Default.Home, 0),
                        Triple("Petugas", Icons.Default.People, 1),
                        Triple("Agenda", Icons.Default.Event, 2),
                        Triple("Kas & Donasi", Icons.Default.AccountBalanceWallet, 3),
                        Triple("Admin", Icons.Default.AdminPanelSettings, 4)
                    )

                    navItems.forEach { (label, icon, tabIndex) ->
                        val isSelected = selectedTab == tabIndex
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tabIndex) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) GoldAccent else Color(0xFF94A3B8)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GoldAccent else Color(0xFF94A3B8)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldAccent,
                                selectedTextColor = GoldAccent,
                                indicatorColor = Color(0xFF0F5A47),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_item_$tabIndex")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabContentRouter(
                selectedTab = selectedTab,
                viewModel = viewModel,
                currentTime = currentTime,
                dateMasehi = dateMasehi,
                dateHijri = dateHijri,
                nextPrayerName = nextPrayerName,
                nextPrayerCountdown = nextPrayerCountdown,
                isIqomahCountingDown = isIqomahCountingDown,
                iqomahPrayerName = iqomahPrayerName,
                iqomahCountdownText = iqomahCountdownText,
                prayerSchedule = prayerSchedule,
                dailyOfficers = dailyOfficers,
                announcements = announcements
            )
        }
    }
}

@Composable
fun TabContentRouter(
    selectedTab: Int,
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
    dailyOfficers: List<com.example.data.model.DailyPrayerOfficer>,
    announcements: List<com.example.data.model.Announcement>
) {
    when (selectedTab) {
        0 -> HomeScreen(
            viewModel = viewModel,
            currentTime = currentTime,
            dateMasehi = dateMasehi,
            dateHijri = dateHijri,
            nextPrayerName = nextPrayerName,
            nextPrayerCountdown = nextPrayerCountdown,
            isIqomahCountingDown = isIqomahCountingDown,
            iqomahPrayerName = iqomahPrayerName,
            iqomahCountdownText = iqomahCountdownText,
            prayerSchedule = prayerSchedule,
            dailyOfficers = dailyOfficers,
            announcements = announcements,
            onNavigateToTab = { viewModel.selectTab(it) },
            onEnterTvMode = { viewModel.setTvMode(true) }
        )
        1 -> OfficersScreen(viewModel = viewModel)
        2 -> AgendaScreen(viewModel = viewModel)
        3 -> FinanceScreen(viewModel = viewModel)
        4 -> AdminDashboardScreen(viewModel = viewModel)
        else -> HomeScreen(
            viewModel = viewModel,
            currentTime = currentTime,
            dateMasehi = dateMasehi,
            dateHijri = dateHijri,
            nextPrayerName = nextPrayerName,
            nextPrayerCountdown = nextPrayerCountdown,
            isIqomahCountingDown = isIqomahCountingDown,
            iqomahPrayerName = iqomahPrayerName,
            iqomahCountdownText = iqomahCountdownText,
            prayerSchedule = prayerSchedule,
            dailyOfficers = dailyOfficers,
            announcements = announcements,
            onNavigateToTab = { viewModel.selectTab(it) },
            onEnterTvMode = { viewModel.setTvMode(true) }
        )
    }
}
