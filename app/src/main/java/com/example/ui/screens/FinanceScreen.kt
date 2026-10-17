package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.data.model.KasItem
import com.example.data.model.TransactionType
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
                        text = "Donasi Digital",
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
                // Donasi Digital
                item {
                    DigitalDonationCard(context = context)
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

@Composable
fun DigitalDonationCard(context: Context) {
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

            Spacer(modifier = Modifier.height(14.dp))

            // Simulated High-res QRIS Box
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "QRIS Masjid At-Taqwa",
                        tint = Color(0xFF0F291E),
                        modifier = Modifier.size(130.dp)
                    )
                    Text(
                        text = "NMID: ID1020260012345",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mendukung Semua Dompet Digital & Mobile Banking",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "BCA Mobile, BSI Mobile, Livin' Mandiri, GoPay, OVO, DANA, ShopeePay, LinkAja",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
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
            Text(
                text = "Rekening Bank Resmi Masjid",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Salurkan infaq & sedekah langsung ke rekening DKM At-Taqwa",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            BankRow(
                bankName = "Bank Syariah Indonesia (BSI)",
                accountNumber = "7123456789",
                accountHolder = "DKM Masjid Jami At-Taqwa Penancangan",
                context = context
            )

            Spacer(modifier = Modifier.height(8.dp))

            BankRow(
                bankName = "Bank Muamalat",
                accountNumber = "1010023344",
                accountHolder = "Masjid Jami At-Taqwa Penancangan",
                context = context
            )

            Spacer(modifier = Modifier.height(8.dp))

            BankRow(
                bankName = "Bank Central Asia (BCA)",
                accountNumber = "5420987654",
                accountHolder = "Panitia Pembangunan At-Taqwa",
                context = context
            )
        }
    }
}

@Composable
fun BankRow(
    bankName: String,
    accountNumber: String,
    accountHolder: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF071911))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = bankName, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = accountNumber, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(text = "a.n. $accountHolder", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("No Rekening", accountNumber)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Nomor rekening $accountNumber disalin!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F5A47))
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

private fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace(",00", "")
}
