package com.example.data.model

data class PrayerTime(
    val id: String,
    val name: String,
    val time: String, // HH:mm
    val arabicName: String,
    val iqomahWaitMinutes: Int = 10
)

data class PrayerDaySchedule(
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String,
    val dateMasehi: String,
    val dateHijri: String,
    val cityName: String = "Penancangan, Serang (Banten)"
) {
    fun toPrayerList(): List<PrayerTime> = listOf(
        PrayerTime("subuh", "Subuh", subuh, "الصبح", 12),
        PrayerTime("terbit", "Terbit", terbit, "الشروق", 0),
        PrayerTime("dzuhur", "Dzuhur", dzuhur, "الظهر", 10),
        PrayerTime("ashar", "Ashar", ashar, "العصر", 10),
        PrayerTime("maghrib", "Maghrib", maghrib, "المغرب", 8),
        PrayerTime("isya", "Isya", isya, "العشاء", 10)
    )
}
