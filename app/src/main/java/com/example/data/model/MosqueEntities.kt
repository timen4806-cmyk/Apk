package com.example.data.model

data class Announcement(
    val id: String,
    val text: String,
    val isActive: Boolean = true,
    val priority: Int = 1,
    val createdAt: String = ""
)

enum class TransactionType {
    PEMASUKAN,
    PENGELUARAN
}

data class KasItem(
    val id: String,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val date: String,
    val notes: String = ""
)

data class AgendaItem(
    val id: String,
    val title: String,
    val speaker: String,
    val date: String, // YYYY-MM-DD
    val time: String, // e.g. "08:00 - 10:00 WIB"
    val location: String,
    val description: String,
    val category: String,
    val reminderMinutesBefore: Int = 60
)

data class GalleryItem(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val description: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null
)

data class DonationConfirmation(
    val id: String,
    val donorName: String,
    val amount: Long,
    val bankDestination: String,
    val date: String,
    val prayerNote: String
)
