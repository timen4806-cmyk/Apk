package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.MainViewModel
import com.example.data.model.KasItem
import com.example.data.model.TransactionType
import com.example.data.util.QrCodeGenerator
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NightSurface
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val kasTransactions by viewModel.kasTransactions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Laporan Kas Masjid, 1: Donasi Digital & QRIS

    // Calculations
    val totalPemasukan = remember(kasTransactions) {
        kasTransactions.filter { it.type == TransactionType.PEMASUKAN }.sumOf { it.amount }
    }
    val totalPengeluaran = remember(kasTransactions) {
        kasTransactions.filter { it.type == TransactionType.PENGELUARAN }.sumOf { it.amount }
    }
    val saldoAkhir = totalPemasukan - totalPengeluaran

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("finance_screen")
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF092017),
            contentColor = GoldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Transparansi Kas",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Donasi Digital & QR",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // Summary Card
                item {
                    KasSummaryCard(
                        saldo = saldoAkhir,
                        pemasukan = totalPemasukan,
                        pengeluaran = totalPengeluaran
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Pemasukan & Pengeluaran",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${kasTransactions.size} Transaksi",
                            color = GoldAccent,
                            fontSize = 12.sp
                        )
                    }
                }

                items(kasTransactions) { item ->
                    KasTransactionRow(item = item)
                }
            } else {
                // Donasi Digital & QR Code
                item {
                    DigitalDonationCard(context = context)
                }

                item {
                    DanaDonationCard(context = context)
                }

                item {
                    DevelopmentProgressBar()
                }

                item {
                    BankTransferCard(context = context)
                }
            }
        }
    }
}

@Composable
fun KasSummaryCard(saldo: Long, pemasukan: Long, pengeluaran: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                .padding(20.dp)
        ) {
            Text(
                text = "SALDO KAS MASJID SAAT INI",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = formatRupiah(saldo),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "DKM Masjid Jami' At-Taqwa Penancangan • Terbuka & Transparan",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF1E4535))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pemasukan
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0x3310B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Total Masuk", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(pemasukan),
                        color = Color(0xFF34D399),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pengeluaran
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Total Keluar", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(pengeluaran),
                        color = Color(0xFFF87171),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KasTransactionRow(item: KasItem) {
    val isPemasukan = item.type == TransactionType.PEMASUKAN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF1E4535), Color(0xFF163729))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPemasukan) Color(0x3310B981) else Color(0x33EF4444)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPemasukan) Icons.Default.CallReceived else Icons.Default.CallMade,
                    contentDescription = null,
                    tint = if (isPemasukan) Color(0xFF34D399) else Color(0xFFF87171),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.category} • ${item.date}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isPemasukan) "+" else "-") + formatRupiah(item.amount),
                    color = if (isPemasukan) Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class BankInfo(
    val id: String,
    val bankName: String,
    val shortName: String,
    val accountNumber: String,
    val accountHolder: String,
    val bankCode: String,
    val branch: String = "KC Serang",
    val brandColor: Color = Color(0xFF00A39D),
    val isEwallet: Boolean = false,
    val appPackage: String? = null
)

val officialMosqueBanks = listOf(
    BankInfo(
        id = "dana",
        bankName = "DANA Dompet Digital",
        shortName = "DANA",
        accountNumber = "0812-3456-7890",
        accountHolder = "DKM Masjid Jami At-Taqwa Penancangan",
        bankCode = "DANA",
        branch = "Akun Bisnis Resmi DKM",
        brandColor = Color(0xFF118EEA),
        isEwallet = true,
        appPackage = "id.dana"
    ),
    BankInfo(
        id = "bsi",
        bankName = "Bank Syariah Indonesia (BSI)",
        shortName = "BSI",
        accountNumber = "7123456789",
        accountHolder = "DKM Masjid Jami At-Taqwa Penancangan",
        bankCode = "451",
        branch = "KC Hasanudin Serang",
        brandColor = Color(0xFF00A39D)
    ),
    BankInfo(
        id = "muamalat",
        bankName = "Bank Muamalat Indonesia",
        shortName = "Muamalat",
        accountNumber = "1010023344",
        accountHolder = "Masjid Jami At-Taqwa Penancangan",
        bankCode = "147",
        branch = "KC Serang",
        brandColor = Color(0xFF7C3AED)
    ),
    BankInfo(
        id = "bca",
        bankName = "Bank Central Asia (BCA)",
        shortName = "BCA",
        accountNumber = "5420987654",
        accountHolder = "Panitia Pembangunan At-Taqwa",
        bankCode = "014",
        branch = "KCU Serang",
        brandColor = Color(0xFF2563EB)
    )
)

@Composable
fun DigitalDonationCard(context: Context) {
    var selectedNominal by remember { mutableLongStateOf(0L) }
    var showZoomDialog by remember { mutableStateOf(false) }

    val nominalChips = listOf(
        0L to "Bebas",
        10000L to "10rb",
        25000L to "25rb",
        50000L to "50rb",
        100000L to "100rb",
        250000L to "250rb"
    )

    val qrisPayload = remember(selectedNominal) {
        QrCodeGenerator.formatQrisPayload(nominal = selectedNominal)
    }

    val qrBitmap = remember(qrisPayload) {
        QrCodeGenerator.generateQrBitmap(content = qrisPayload, size = 512)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFF10B981)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F5A47))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "QRIS RESMI MASJID JAMI' AT-TAQWA",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Nominal Selection Chips
            Text(
                text = "Pilih Nominal Infaq Cepat:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(nominalChips) { (amount, label) ->
                    val isSelected = selectedNominal == amount
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedNominal = amount },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = Color(0xFF071911),
                            containerColor = Color(0xFF071911),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) GoldAccent else Color(0xFF1E4535)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamically Generated High-res QRIS Box via ZXing library
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(2.dp, GoldAccent, RoundedCornerShape(14.dp))
                    .clickable { showZoomDialog = true }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QRIS Dinamis Masjid At-Taqwa",
                            modifier = Modifier.size(160.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QRIS Masjid At-Taqwa",
                            tint = Color(0xFF0F291E),
                            modifier = Modifier.size(140.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NMID: ID1020260012345",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedNominal > 0) {
                        Text(
                            text = "Nominal: ${formatRupiah(selectedNominal)}",
                            color = Color(0xFF0F5A47),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ketuk QR Code di atas untuk memperbesar tampilan scan layar penuh",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Redirection & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Donasi QRIS Masjid Jami' At-Taqwa Penancangan")
                            putExtra(Intent.EXTRA_TEXT, qrisPayload)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Buka Pembayaran / Bagikan Donasi"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showZoomDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, GoldAccent))),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perbesar QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Mendukung Semua Dompet Digital & Mobile Banking",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "BSI Mobile, Livin' Mandiri, BCA Mobile, GoPay, OVO, DANA, ShopeePay, LinkAja",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }

    if (showZoomDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showZoomDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071911)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFF10B981)))),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan QRIS Donasi Masjid",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Masjid Jami' At-Taqwa Penancangan - Serang",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QRIS Dinamis Full",
                            modifier = Modifier.size(240.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedNominal > 0) {
                        Text(
                            text = "Nominal Terkunci: ${formatRupiah(selectedNominal)}",
                            color = Color(0xFF34D399),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Nominal Bebas (Sesuai Keikhlasan)",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showZoomDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup", color = Color(0xFF071911), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DanaDonationCard(context: Context) {
    var showDanaQrDialog by remember { mutableStateOf(false) }
    val danaInfo = remember {
        officialMosqueBanks.firstOrNull { it.id == "dana" } ?: BankInfo(
            id = "dana",
            bankName = "DANA Dompet Digital",
            shortName = "DANA",
            accountNumber = "0812-3456-7890",
            accountHolder = "DKM Masjid Jami At-Taqwa Penancangan",
            bankCode = "DANA",
            branch = "Akun Bisnis Resmi DKM",
            brandColor = Color(0xFF118EEA),
            isEwallet = true,
            appPackage = "id.dana"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF118EEA), Color(0xFF0284C7)))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF118EEA))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DANA",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Donasi Praktis via DANA",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kirim infaq & sedekah langsung ke akun DANA masjid",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C2B47))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Instant Pay",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account & Phone Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF071927))
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nomor Akun / HP DANA Resmi:",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "0812-3456-7890",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "a.n. DKM Masjid Jami At-Taqwa Penancangan",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Nomor DANA Masjid At-Taqwa", "081234567890")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Nomor DANA (081234567890) berhasil disalin!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF118EEA))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin Nomor DANA",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Launch DANA App directly
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Nomor DANA Masjid At-Taqwa", "081234567890")
                        clipboard.setPrimaryClip(clip)

                        val launchIntent = context.packageManager.getLaunchIntentForPackage("id.dana")
                        if (launchIntent != null) {
                            Toast.makeText(context, "Nomor DANA disalin! Membuka aplikasi DANA...", Toast.LENGTH_SHORT).show()
                            context.startActivity(launchIntent)
                        } else {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Donasi DANA - Masjid Jami' At-Taqwa")
                                putExtra(Intent.EXTRA_TEXT, "DONASI INFAQ VIA DANA MASJID JAMI' AT-TAQWA\nNomor DANA: 081234567890\nAtas Nama: DKM Masjid Jami At-Taqwa Penancangan\nKeperluan: Infaq & Sedekah Kas Masjid")
                            }
                            Toast.makeText(context, "Nomor DANA (081234567890) disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent.createChooser(shareIntent, "Buka Pembayaran DANA"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF118EEA)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka App DANA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Show DANA QR Code
                OutlinedButton(
                    onClick = { showDanaQrDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF118EEA)))
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lihat QR DANA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDanaQrDialog) {
        BankQrDialog(
            bank = danaInfo,
            context = context,
            onDismiss = { showDanaQrDialog = false }
        )
    }
}

@Composable
fun DevelopmentProgressBar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841)))
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
                Text(
                    text = "Renovasi Menara & Kanopi Sayap",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "59% Terkumpul",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { 0.59f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = GoldAccent,
                trackColor = Color(0xFF071911)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terkumpul: Rp 89.500.000",
                    color = Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Target: Rp 150.000.000",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun BankTransferCard(context: Context) {
    var selectedBankForQr by remember { mutableStateOf<BankInfo?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF1E4535), Color(0xFF135841)))
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rekening Bank Resmi Masjid",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ketuk 'QR Code' untuk scan transfer langsung atau salin nomor rekening",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F5A47))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "QR Dinamis",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            officialMosqueBanks.forEach { bank ->
                BankRow(
                    bank = bank,
                    context = context,
                    onShowQr = { selectedBankForQr = bank }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    selectedBankForQr?.let { bank ->
        BankQrDialog(
            bank = bank,
            context = context,
            onDismiss = { selectedBankForQr = null }
        )
    }
}

@Composable
fun BankRow(
    bank: BankInfo,
    context: Context,
    onShowQr: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF071911))
            .border(1.dp, Color(0xFF1E4535), RoundedCornerShape(10.dp))
            .clickable { onShowQr() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bank.brandColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bank.shortName,
                        color = bank.brandColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = bank.bankName,
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bank.accountNumber,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "a.n. ${bank.accountHolder}",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Tombol Generate / Tampilkan QR Dinamis
            IconButton(
                onClick = onShowQr,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F5A47))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Tampilkan QR Bank",
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Tombol Salin Nomor Rekening
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("No Rekening ${bank.bankName}", bank.accountNumber)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Nomor rekening ${bank.bankName} (${bank.accountNumber}) berhasil disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E4535))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Salin No Rekening",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Interactive dialog displaying a dynamically generated QR Code via ZXing library
 * from the selected bank account details, supporting real-time nominal update and
 * rapid redirection to banking apps or sharing.
 */
@Composable
fun BankQrDialog(
    bank: BankInfo,
    context: Context,
    onDismiss: () -> Unit
) {
    var selectedNominal by remember { mutableLongStateOf(0L) }

    val nominalOptions = listOf(
        0L to "Bebas",
        20000L to "20rb",
        50000L to "50rb",
        100000L to "100rb",
        250000L to "250rb",
        500000L to "500rb"
    )

    // Formatted payload containing all details for scanner
    val bankPayload = remember(bank, selectedNominal) {
        QrCodeGenerator.formatBankPayload(
            bankName = bank.bankName,
            accountNumber = bank.accountNumber,
            accountHolder = bank.accountHolder,
            nominal = selectedNominal,
            purpose = "Infaq & Sedekah Masjid Jami' At-Taqwa Penancangan"
        )
    }

    // Dynamic QR Bitmap generated via ZXing library
    val qrBitmap = remember(bankPayload) {
        QrCodeGenerator.generateQrBitmap(content = bankPayload, size = 512)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF071911)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(GoldAccent, bank.brandColor))
            ),
            modifier = Modifier.padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bank.brandColor.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = bank.shortName,
                            color = bank.brandColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "QR Transfer Bank",
                        color = GoldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = bank.bankName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kode Bank: ${bank.bankCode} • ${bank.branch}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nominal Selector
                Text(
                    text = "Pilih Nominal Infaq (Otomatis Terkunci di QR):",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(nominalOptions) { (amt, label) ->
                        val isSelected = selectedNominal == amt
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedNominal = amt },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color(0xFF071911),
                                containerColor = Color(0xFF0F291E),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Dynamically Generated QR Code via ZXing
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, bank.brandColor, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code ${bank.bankName}",
                            modifier = Modifier.size(190.dp)
                        )
                    } else {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Number & Copy Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F291E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bank.accountNumber,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "a.n. ${bank.accountHolder}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("No Rekening", bank.accountNumber)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Nomor rekening ${bank.accountNumber} disalin!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pindai QR ini dari aplikasi mobile banking / kamera HP jamaah lain untuk transfer kilat",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action: Redirection to banking / payment apps / DANA
                Button(
                    onClick = {
                        val cleanAcc = bank.accountNumber.replace("-", "").trim()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("No ${bank.bankName}", cleanAcc)
                        clipboard.setPrimaryClip(clip)

                        if (bank.appPackage != null) {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(bank.appPackage)
                            if (launchIntent != null) {
                                Toast.makeText(context, "Nomor ${bank.shortName} ($cleanAcc) disalin! Membuka ${bank.shortName}...", Toast.LENGTH_SHORT).show()
                                context.startActivity(launchIntent)
                                return@Button
                            }
                        }

                        Toast.makeText(context, "Nomor ${bank.accountNumber} disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Donasi ${bank.bankName} - Masjid Jami' At-Taqwa")
                            putExtra(Intent.EXTRA_TEXT, bankPayload)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Buka Aplikasi Pembayaran / Bagikan"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = bank.brandColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (bank.isEwallet) "Buka Aplikasi ${bank.shortName} / Bayar" else "Buka Aplikasi Bank / Bayar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace(",00", "")
}
