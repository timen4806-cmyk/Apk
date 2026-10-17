package com.example.data.model

data class DailyPrayerOfficer(
    val id: String,
    val prayerName: String, // Subuh, Dzuhur, Ashar, Maghrib, Isya
    val imam: String,
    val muadzin: String,
    val bilal: String,
    val notes: String = "Siap Bertugas",
    val isConfirmed: Boolean = true
)

data class FridaySchedule(
    val id: String,
    val date: String,
    val khatib: String,
    val imam: String,
    val muadzin: String,
    val bilal: String,
    val khutbahTitle: String,
    val time: String = "11:45 WIB"
)

data class EidSchedule(
    val id: String,
    val eidType: String, // "Idul Fitri 1448 H" atau "Idul Adha 1448 H"
    val date: String,
    val time: String = "06:45 WIB",
    val khatib: String,
    val imam: String,
    val bilal: String,
    val location: String = "Ruang Utama & Pelataran Masjid Jami' At-Taqwa",
    val notes: String = "Jamaah diimbau membawa sajadah dan berwudhu dari rumah"
)
