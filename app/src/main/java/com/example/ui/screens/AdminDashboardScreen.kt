package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.DailyPrayerOfficer
import com.example.data.model.TransactionType
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NightSurface

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dailyOfficers by viewModel.dailyOfficers.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val isCloudSynced by viewModel.isCloudSynced.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    // Security States
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isLockedOut by viewModel.isLockedOut.collectAsState()
    val lockoutTimerSeconds by viewModel.lockoutTimerSeconds.collectAsState()
    val failedAttempts by viewModel.failedAttempts.collectAsState()

    // Dialog States
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showEditOfficerDialog by remember { mutableStateOf<DailyPrayerOfficer?>(null) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }
    var showAddKasDialog by remember { mutableStateOf(false) }
    var showAddAgendaDialog by remember { mutableStateOf(false) }
    var showAddGalleryDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    if (!isAdminLoggedIn) {
        // High-Security Admin Gate (PIN Protection for public web access)
        AdminLoginGate(
            isLockedOut = isLockedOut,
            lockoutSeconds = lockoutTimerSeconds,
            failedAttempts = failedAttempts,
            onVerifyPin = { pin ->
                val success = viewModel.verifyAdminPin(pin)
                if (success) {
                    Toast.makeText(context, "Akses Pengurus DKM Terverifikasi!", Toast.LENGTH_SHORT).show()
                } else if (!isLockedOut) {
                    Toast.makeText(context, "PIN salah! Sisa percobaan: ${3 - failedAttempts}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    } else {
        // Authenticated Admin Dashboard
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("admin_dashboard_screen"),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Security Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFF10B981)))
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F5A47)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Dashboard Pengurus DKM",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sesi Terverifikasi • Akses Cloud Aman",
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Action: Logout / Lock & Change PIN
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { showChangePinDialog = true },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF13382B))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = "Ganti PIN",
                                        tint = GoldAccent
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.logoutAdmin()
                                        Toast.makeText(context, "Sesi Admin Ditutup.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33EF4444))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kunci Sesi",
                                        tint = Color(0xFFF87171)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: $lastSyncTime",
                                color = Color(0xFF6EE7B7),
                                fontSize = 11.sp
                            )
                            Button(
                                onClick = {
                                    viewModel.triggerCloudSync()
                                    Toast.makeText(context, "Data berhasil disinkronkan ke Cloud!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Sync Cloud", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Action 1: Broadcast Push Notification
            item {
                AdminActionCard(
                    title = "Kirim Pesan Siaran Push Notification",
                    description = "Kirim pesan siaran instan kepada seluruh jemaah masjid secara real-time.",
                    buttonText = "Buat Siaran Baru",
                    icon = Icons.Default.Campaign,
                    onClick = { showBroadcastDialog = true }
                )
            }

            // Action 2: Kelola Petugas Shalat 5 Waktu
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841))))
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
                            Text(
                                text = "Kelola Petugas Shalat 5 Waktu",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap untuk Edit",
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        dailyOfficers.forEach { officer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF071911))
                                    .clickable { showEditOfficerDialog = officer }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = officer.prayerName,
                                        color = GoldAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Imam: ${officer.imam} • Muadzin: ${officer.muadzin}",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Action 3: Kelola Running Text
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841))))
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
                            Text(
                                text = "Kelola Running Text (Marquee)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalButton(
                                onClick = { showAddAnnouncementDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF0F5A47), contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Tambah", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        announcements.forEach { ann ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF071911))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = ann.isActive,
                                    onCheckedChange = { viewModel.toggleAnnouncement(ann.id) },
                                    modifier = Modifier.scale(0.8f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ann.text,
                                    color = if (ann.isActive) Color.White else Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteAnnouncement(ann.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Action 4: Catat Transaksi Kas Baru
            item {
                AdminActionCard(
                    title = "Catat Transaksi Kas Masjid",
                    description = "Input pemasukan infaq/sedekah atau pengeluaran operasional masjid.",
                    buttonText = "Tambah Transaksi Kas",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = { showAddKasDialog = true }
                )
            }

            // Action 5: Tambah Agenda / Pengajian Baru
            item {
                AdminActionCard(
                    title = "Tambah Jadwal Agenda & Pengajian",
                    description = "Publikasikan jadwal kajian baru untuk disinkronkan ke kalender jemaah.",
                    buttonText = "Tambah Agenda Baru",
                    icon = Icons.Default.Event,
                    onClick = { showAddAgendaDialog = true }
                )
            }

            // Action 6: Unggah Dokumentasi Galeri
            item {
                AdminActionCard(
                    title = "Unggah Dokumentasi Galeri Foto",
                    description = "Unggah dokumentasi acara & foto kegiatan masjid terbaru.",
                    buttonText = "Tambah Foto Dokumentasi",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = { showAddGalleryDialog = true }
                )
            }
        }
    }

    // Dialog: Ubah PIN Keamanan
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Ubah PIN Keamanan DKM", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Pastikan PIN baru mudah diingat oleh pengurus resmi.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("PIN Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("PIN Baru (min. 4 angka)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.changeAdminPin(oldPin, newPin)
                        if (success) {
                            showChangePinDialog = false
                            Toast.makeText(context, "PIN Keamanan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN Lama salah atau PIN baru kurang dari 4 angka!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Simpan PIN Baru")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Broadcast Push Notification
    if (showBroadcastDialog) {
        var title by remember { mutableStateOf("Pengumuman Masjid Jami' At-Taqwa") }
        var message by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Kirim Siaran ke Jemaah", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Pesan ini akan langsung muncul sebagai Push Notification di HP jemaah.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Siaran") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Isi Pesan Siaran") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.sendBroadcastNotification(title, message)
                            showBroadcastDialog = false
                            Toast.makeText(context, "Pesan siaran berhasil dikirim ke seluruh jemaah!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Kirim Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Edit Petugas Shalat
    showEditOfficerDialog?.let { officer ->
        var imam by remember { mutableStateOf(officer.imam) }
        var muadzin by remember { mutableStateOf(officer.muadzin) }
        var bilal by remember { mutableStateOf(officer.bilal) }
        var notes by remember { mutableStateOf(officer.notes) }

        AlertDialog(
            onDismissRequest = { showEditOfficerDialog = null },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Edit Petugas Shalat ${officer.prayerName}", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = imam,
                        onValueChange = { imam = it },
                        label = { Text("Nama Imam") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = muadzin,
                        onValueChange = { muadzin = it },
                        label = { Text("Nama Muadzin") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = bilal,
                        onValueChange = { bilal = it },
                        label = { Text("Nama Bilal") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Status / Catatan") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDailyOfficer(officer.copy(imam = imam, muadzin = muadzin, bilal = bilal, notes = notes))
                        showEditOfficerDialog = null
                        Toast.makeText(context, "Jadwal petugas shalat ${officer.prayerName} diperbarui!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOfficerDialog = null }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Tambah Announcement
    if (showAddAnnouncementDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAnnouncementDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Tambah Running Text", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Isi Pesan Teks Berjalan") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.addAnnouncement(text)
                            showAddAnnouncementDialog = false
                            Toast.makeText(context, "Running text berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAnnouncementDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Tambah Kas
    if (showAddKasDialog) {
        var title by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var isPemasukan by remember { mutableStateOf(true) }
        var category by remember { mutableStateOf("Infaq Jum'at") }

        AlertDialog(
            onDismissRequest = { showAddKasDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Catat Transaksi Kas", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isPemasukan,
                            onClick = { isPemasukan = true },
                            label = { Text("Pemasukan") }
                        )
                        FilterChip(
                            selected = !isPemasukan,
                            onClick = { isPemasukan = false },
                            label = { Text("Pengeluaran") }
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Keterangan Transaksi") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountStr.toLongOrNull() ?: 0L
                        if (title.isNotBlank() && amount > 0L) {
                            viewModel.addKasTransaction(
                                title = title,
                                amount = amount,
                                type = if (isPemasukan) TransactionType.PEMASUKAN else TransactionType.PENGELUARAN,
                                category = category
                            )
                            showAddKasDialog = false
                            Toast.makeText(context, "Transaksi kas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKasDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Tambah Agenda
    if (showAddAgendaDialog) {
        var title by remember { mutableStateOf("") }
        var speaker by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("Ahad Depan") }
        var time by remember { mutableStateOf("08:00 - 10:00 WIB") }
        var location by remember { mutableStateOf("Ruang Utama Masjid At-Taqwa") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAgendaDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Tambah Agenda Pengajian", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Acara / Tema Kajian") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = speaker,
                        onValueChange = { speaker = it },
                        label = { Text("Penceramah / Ustadz") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Hari / Tanggal") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Waktu") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Deskripsi Singkat") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && speaker.isNotBlank()) {
                            viewModel.addAgenda(title, speaker, date, time, location, desc, "Kajian Rutin")
                            showAddAgendaDialog = false
                            Toast.makeText(context, "Agenda baru berhasil dipublikasikan!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAgendaDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Tambah Dokumentasi Galeri
    if (showAddGalleryDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Kegiatan Masjid") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGalleryDialog = false },
            containerColor = Color(0xFF092017),
            title = { Text(text = "Tambah Foto Dokumentasi", color = GoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Acara / Dokumentasi") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori Acara") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Keterangan Foto") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addGalleryItem(title, category, desc)
                            showAddGalleryDialog = false
                            Toast.makeText(context, "Dokumentasi kegiatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                ) {
                    Text("Tambahkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGalleryDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun AdminLoginGate(
    isLockedOut: Boolean,
    lockoutSeconds: Int,
    failedAttempts: Int,
    onVerifyPin: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFF10B981))),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security Shield Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F3A2B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLockedOut) Icons.Default.LockClock else Icons.Default.Security,
                        contentDescription = "Keamanan",
                        tint = if (isLockedOut) Color(0xFFEF4444) else GoldAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Akses Pengurus DKM Terproteksi",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aplikasi ini dapat diakses publik oleh seluruh jemaah melalui link web. Untuk mengubah jadwal petugas, running text, kas, atau siaran pengumuman, silakan masukkan PIN Keamanan DKM.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isLockedOut) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33EF4444))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Terlalu banyak percobaan salah! Keamanan terkunci: coba lagi dalam $lockoutSeconds detik.",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it },
                        label = { Text("Masukkan PIN Keamanan") },
                        singleLine = true,
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle PIN",
                                    tint = GoldAccent
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (failedAttempts > 0) {
                        Text(
                            text = "Percobaan salah: $failedAttempts dari 3 batas maksimal",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = "PIN Standar Awal Pengurus: 2026 (dapat diubah kapan saja)",
                        color = Color(0xFF6EE7B7),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (pinInput.isNotBlank()) {
                                onVerifyPin(pinInput)
                                pinInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Buka Akses Pengurus", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminActionCard(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F5A47)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size(40.dp)
)
