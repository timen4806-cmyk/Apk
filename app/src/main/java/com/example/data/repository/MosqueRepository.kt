package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MosqueRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("masjid_attaqwa_prefs", Context.MODE_PRIVATE)

    // Daily Officer Schedules
    private val _dailyOfficers = MutableStateFlow<List<DailyPrayerOfficer>>(loadDailyOfficers())
    val dailyOfficers: StateFlow<List<DailyPrayerOfficer>> = _dailyOfficers.asStateFlow()

    // Friday Schedule
    private val _fridaySchedule = MutableStateFlow<FridaySchedule>(loadFridaySchedule())
    val fridaySchedule: StateFlow<FridaySchedule> = _fridaySchedule.asStateFlow()

    // Eid Schedules
    private val _eidSchedules = MutableStateFlow<List<EidSchedule>>(loadEidSchedules())
    val eidSchedules: StateFlow<List<EidSchedule>> = _eidSchedules.asStateFlow()

    // Iqomah Durations (in minutes) for each prayer
    private val _iqomahDurations = MutableStateFlow<Map<String, Int>>(loadIqomahDurations())
    val iqomahDurations: StateFlow<Map<String, Int>> = _iqomahDurations.asStateFlow()

    // Hijri Adjustment Offset (in days, e.g. -2, -1, 0, +1, +2)
    private val _hijriOffset = MutableStateFlow(prefs.getInt("hijri_day_offset", 0))
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    // Announcements / Running Text
    private val _announcements = MutableStateFlow<List<Announcement>>(getInitialAnnouncements())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Financial Kas Items
    private val _kasTransactions = MutableStateFlow<List<KasItem>>(getInitialKasItems())
    val kasTransactions: StateFlow<List<KasItem>> = _kasTransactions.asStateFlow()

    // Agendas / Kajian
    private val _agendas = MutableStateFlow<List<AgendaItem>>(getInitialAgendas())
    val agendas: StateFlow<List<AgendaItem>> = _agendas.asStateFlow()

    // Gallery Items
    private val _galleryItems = MutableStateFlow<List<GalleryItem>>(getInitialGalleryItems())
    val galleryItems: StateFlow<List<GalleryItem>> = _galleryItems.asStateFlow()

    // Cloud Sync State
    private val _isCloudSynced = MutableStateFlow(true)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Hari ini, 17:40 WIB (Tersinkron)")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // Daily Officer Management
    fun updateDailyOfficer(updated: DailyPrayerOfficer) {
        val current = _dailyOfficers.value.toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            _dailyOfficers.value = current
            saveDailyOfficers(current)
            triggerCloudSync()
        }
    }

    // Friday Schedule Management
    fun updateFridaySchedule(updated: FridaySchedule) {
        _fridaySchedule.value = updated
        saveFridaySchedule(updated)
        triggerCloudSync()
    }

    // Eid Schedule Management
    fun updateEidSchedule(updated: EidSchedule) {
        val current = _eidSchedules.value.toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            _eidSchedules.value = current
            saveEidSchedules(current)
            triggerCloudSync()
        }
    }

    // Iqomah Duration Management (in minutes)
    fun updateIqomahDuration(prayerName: String, minutes: Int) {
        val current = _iqomahDurations.value.toMutableMap()
        current[prayerName] = minutes.coerceAtLeast(1)
        _iqomahDurations.value = current
        saveIqomahDurations(current)
        triggerCloudSync()
    }

    // Hijri Offset Adjustment Management (e.g. -2..+2)
    fun updateHijriOffset(offset: Int) {
        _hijriOffset.value = offset
        prefs.edit().putInt("hijri_day_offset", offset).apply()
        triggerCloudSync()
    }

    // Running Text Announcements Management
    fun addAnnouncement(text: String) {
        val newAnnouncement = Announcement(
            id = UUID.randomUUID().toString(),
            text = text,
            isActive = true,
            priority = 1,
            createdAt = getCurrentTimeString()
        )
        _announcements.value = listOf(newAnnouncement) + _announcements.value
        triggerCloudSync()
    }

    fun toggleAnnouncement(id: String) {
        _announcements.value = _announcements.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        triggerCloudSync()
    }

    fun deleteAnnouncement(id: String) {
        _announcements.value = _announcements.value.filterNot { it.id == id }
        triggerCloudSync()
    }

    // Kas Management
    fun addKasTransaction(
        title: String,
        amount: Long,
        type: TransactionType,
        category: String,
        notes: String = ""
    ) {
        val newItem = KasItem(
            id = UUID.randomUUID().toString(),
            title = title,
            amount = amount,
            type = type,
            category = category,
            date = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date()),
            notes = notes
        )
        _kasTransactions.value = listOf(newItem) + _kasTransactions.value
        triggerCloudSync()
    }

    fun deleteKasTransaction(id: String) {
        _kasTransactions.value = _kasTransactions.value.filterNot { it.id == id }
        triggerCloudSync()
    }

    // Agenda Management
    fun addAgenda(
        title: String,
        speaker: String,
        date: String,
        time: String,
        location: String,
        description: String,
        category: String
    ) {
        val newAgenda = AgendaItem(
            id = UUID.randomUUID().toString(),
            title = title,
            speaker = speaker,
            date = date,
            time = time,
            location = location,
            description = description,
            category = category
        )
        _agendas.value = listOf(newAgenda) + _agendas.value
        triggerCloudSync()
    }

    fun deleteAgenda(id: String) {
        _agendas.value = _agendas.value.filterNot { it.id == id }
        triggerCloudSync()
    }

    // Gallery Management
    fun addGalleryItem(title: String, category: String, description: String, date: String = "") {
        val item = GalleryItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            description = description,
            date = if (date.isNotBlank()) date else SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        )
        _galleryItems.value = listOf(item) + _galleryItems.value
        triggerCloudSync()
    }

    // Trigger Cloud Sync simulation
    fun triggerCloudSync() {
        _isCloudSynced.value = false
        val timeNow = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date())
        _lastSyncTime.value = "Menyinkronkan ke Cloud..."
        // Mark as synced
        _isCloudSynced.value = true
        _lastSyncTime.value = "Hari ini, $timeNow WIB (Tersinkron Cloud)"
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())
    }

    private fun getInitialDailyOfficers(): List<DailyPrayerOfficer> {
        return listOf(
            DailyPrayerOfficer("subuh", "Subuh", "K.H. Tb. Abdul Qodir", "Ust. M. Syukron", "Ust. Ahmad Rifai", "Siap Bertugas", true),
            DailyPrayerOfficer("dzuhur", "Dzuhur", "Ust. Drs. H. Marzuki", "Kang Fajar Siddiq", "Kang Wildan Firdaus", "Siap Bertugas", true),
            DailyPrayerOfficer("ashar", "Ashar", "Ust. H. Nuruddin, S.Ag.", "Kang Wildan Firdaus", "Kang Fajar Siddiq", "Siap Bertugas", true),
            DailyPrayerOfficer("maghrib", "Maghrib", "K.H. Tb. Abdul Qodir", "Ust. Ahmad Rifai", "Ust. M. Syukron", "Siap Bertugas", true),
            DailyPrayerOfficer("isya", "Isya", "Ust. Dr. H. Ahmad Fauzi, M.A.", "Ust. Ahmad Rifai", "Ust. M. Syukron", "Siap Bertugas", true)
        )
    }

    private fun getInitialFridaySchedule(): FridaySchedule {
        return FridaySchedule(
            id = "friday_current",
            date = "Jum'at Pekan Ini",
            khatib = "K.H. Tb. Abdul Qodir, M.Pd.I",
            imam = "Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
            muadzin = "Ust. M. Syukron",
            bilal = "Ust. Ahmad Rifai",
            khutbahTitle = "Meneguhkan Ukhuwah Islamiyah dan Kesalehan Sosial di Era Digital",
            time = "11:45 WIB (Adzan Pertama)"
        )
    }

    private fun getInitialEidSchedules(): List<EidSchedule> {
        return listOf(
            EidSchedule(
                id = "eid_fitri",
                eidType = "Shalat Idul Fitri 1448 H",
                date = "1 Syawal 1448 H",
                time = "06:30 WIB",
                khatib = "Prof. Dr. K.H. Tb. Mansyur, M.A.",
                imam = "K.H. Tb. Abdul Qodir",
                bilal = "Ust. Ahmad Rifai & Kang Wildan",
                location = "Gedung Utama, Lantai 2 & Halaman Masjid Jami' At-Taqwa",
                notes = "Infaq Shalat Idul Fitri disalurkan untuk kas yatim dan fakir miskin lingkungan Penancangan"
            ),
            EidSchedule(
                id = "eid_adha",
                eidType = "Shalat Idul Adha 1448 H",
                date = "10 Dzulhijjah 1448 H",
                time = "06:15 WIB",
                khatib = "Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
                imam = "Ust. Drs. H. Marzuki",
                bilal = "Ust. M. Syukron",
                location = "Ruang Utama & Pelataran Masjid Jami' At-Taqwa Penancangan",
                notes = "Penyembelihan hewan kurban dilaksanakan ba'da shalat di area sentra kurban masjid"
            )
        )
    }

    private fun getInitialAnnouncements(): List<Announcement> {
        return listOf(
            Announcement("1", "Selamat datang di Masjid Jami' At-Taqwa Penancangan. Luruskan dan rapatkan shaf demi kesempurnaan shalat berjamaah.", true, 1),
            Announcement("2", "Mohon menonaktifkan atau mengheningkan nada dering handphone (HP) selama berada di dalam masjid.", true, 1),
            Announcement("3", "Kajian Rutin Malam Kamis ba'da Maghrib: Kitab Riyadhus Shalihin bersama Ust. Dr. H. Ahmad Fauzi, Lc., M.A.", true, 2),
            Announcement("4", "Laporan Kas Masjid: Saldo Akhir Rp 48.750.000,-. Terima kasih atas infaq dan sedekah Bapak/Ibu Jamaah. Jazakumullahu khairan.", true, 1),
            Announcement("5", "Pembangunan Kanopi & Penataan Area Wudhu Sayap Timur sedang berjalan. Salurkan donasi Anda melalui Rekening BSI atau QRIS.", true, 2)
        )
    }

    private fun getInitialKasItems(): List<KasItem> {
        return listOf(
            KasItem("k1", "Infaq Kotak Shalat Jum'at", 6850000, TransactionType.PEMASUKAN, "Infaq Jum'at", "10 Okt 2026", "Kotak keliling & kotak tetap"),
            KasItem("k2", "Sedekah Subuh Berkah", 2150000, TransactionType.PEMASUKAN, "Sedekah Subuh", "09 Okt 2026", "Kaleng Subuh Jamaah"),
            KasItem("k3", "Pembayaran Listrik PLN & Air PDAM", 1850000, TransactionType.PENGELUARAN, "Operasional Rutin", "08 Okt 2026", "Tagihan bulanan"),
            KasItem("k4", "Donasi Renovasi Area Wudhu (Hamba Allah)", 15000000, TransactionType.PEMASUKAN, "Donasi Pembangunan", "05 Okt 2026", "Transfer BSI"),
            KasItem("k5", "Honor Marbot, Muadzin & Petugas Kebersihan", 3500000, TransactionType.PENGELUARAN, "Honorarium", "01 Okt 2026", "Gaji bulanan 3 marbot"),
            KasItem("k6", "Perawatan AC & Penggantian Mic Wireless", 1250000, TransactionType.PENGELUARAN, "Perawatan", "28 Sep 2026", "Servis 4 unit AC")
        )
    }

    private fun getInitialAgendas(): List<AgendaItem> {
        return listOf(
            AgendaItem(
                id = "ag1",
                title = "Kajian Tafsir Al-Qur'an & Tadabbur",
                speaker = "K.H. Tb. Abdul Qodir",
                date = "Setiap Ahad Pagi",
                time = "06:00 - 07:30 WIB",
                location = "Ruang Utama Masjid At-Taqwa",
                description = "Kajian tafsir tematik juz 'Amma disertai sarapan bubur berkah bersama jamaah.",
                category = "Kajian Rutin"
            ),
            AgendaItem(
                id = "ag2",
                title = "Kajian Fiqih Ibadah & Riyadhus Shalihin",
                speaker = "Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
                date = "Setiap Rabu Malam (Ba'da Maghrib)",
                time = "18:20 - 19:15 WIB",
                location = "Ruang Utama Masjid At-Taqwa",
                description = "Membahas fiqih praktis shalat, thaharah, zakat dan bab akhlak mulia.",
                category = "Kajian Rutin"
            ),
            AgendaItem(
                id = "ag3",
                title = "Tahsin & Tahfidz Al-Qur'an Remaja & Dewasa",
                speaker = "Ust. M. Syukron (Qari Nasional)",
                date = "Setiap Selasa & Kamis Ba'da Ashar",
                time = "16:00 - 17:15 WIB",
                location = "Serambi Kanan Masjid",
                description = "Bimbingan makhraj huruf, tajwid tilawah, dan murajaah hafalan santri.",
                category = "Pendidikan Al-Qur'an"
            ),
            AgendaItem(
                id = "ag4",
                title = "Peringatan Maulid Nabi Muhammad SAW",
                speaker = "Habib Muhammad Al-Haddad",
                date = "Sabtu Malam Ahad (Pekan Depan)",
                time = "19:30 - 22:00 WIB",
                location = "Pelataran & Aula Masjid At-Taqwa",
                description = "Tabligh akbar, pembacaan Maulid Simthuddurar, dan santunan 50 anak yatim piatu Penancangan.",
                category = "PHBI"
            )
        )
    }

    private fun getInitialGalleryItems(): List<GalleryItem> {
        return listOf(
            GalleryItem(
                id = "g1",
                title = "Shalat Tarawih & Malam I'tikaf Berjamaah",
                date = "Ramadhan 1447 H",
                category = "Ibadah Ramadhan",
                description = "Antusiasme jamaah memadati ruang shalat utama hingga pelataran luar dalam kekhusyukan malam ganjil."
            ),
            GalleryItem(
                id = "g2",
                title = "Santunan Anak Yatim & Dhuafa Warga Penancangan",
                date = "Muharram 1448 H",
                category = "Sosial & Ziswaf",
                description = "Penyaluran dana santunan kas yatim kepada 65 penerima manfaat di lingkungan RW 01 - RW 06 Penancangan."
            ),
            GalleryItem(
                id = "g3",
                title = "Penyembelihan & Distribusi Kurban Idul Adha",
                date = "Dzulhijjah 1447 H",
                category = "Idul Adha",
                description = "Penyembelihan 12 ekor sapi dan 28 kambing secara higienis dan pembagian paket daging kurban door-to-door."
            ),
            GalleryItem(
                id = "g4",
                title = "Kerja Bakti & Penanaman Pohon Kurma Halaman Masjid",
                date = "Agustus 2026",
                category = "Gotong Royong",
                description = "Aksi bersih-bersih karpet masjid, area wudhu, dan penataan taman hijau oleh RISMA (Remaja Islam Masjid At-Taqwa)."
            )
        )
    }

    // Persistence Helpers
    private fun loadDailyOfficers(): List<DailyPrayerOfficer> {
        val jsonStr = prefs.getString("saved_daily_officers", null) ?: return getInitialDailyOfficers()
        return try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<DailyPrayerOfficer>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DailyPrayerOfficer(
                        id = obj.getString("id"),
                        prayerName = obj.getString("prayerName"),
                        imam = obj.getString("imam"),
                        muadzin = obj.getString("muadzin"),
                        bilal = obj.getString("bilal"),
                        notes = obj.optString("notes", "Siap Bertugas"),
                        isConfirmed = obj.optBoolean("isConfirmed", true)
                    )
                )
            }
            if (list.isNotEmpty()) list else getInitialDailyOfficers()
        } catch (e: Exception) {
            getInitialDailyOfficers()
        }
    }

    private fun saveDailyOfficers(list: List<DailyPrayerOfficer>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("prayerName", item.prayerName)
                obj.put("imam", item.imam)
                obj.put("muadzin", item.muadzin)
                obj.put("bilal", item.bilal)
                obj.put("notes", item.notes)
                obj.put("isConfirmed", item.isConfirmed)
                array.put(obj)
            }
            prefs.edit().putString("saved_daily_officers", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFridaySchedule(): FridaySchedule {
        val jsonStr = prefs.getString("saved_friday_schedule", null) ?: return getInitialFridaySchedule()
        return try {
            val obj = org.json.JSONObject(jsonStr)
            FridaySchedule(
                id = obj.optString("id", "friday_current"),
                date = obj.optString("date", "Jum'at Pekan Ini"),
                khatib = obj.getString("khatib"),
                imam = obj.getString("imam"),
                muadzin = obj.getString("muadzin"),
                bilal = obj.getString("bilal"),
                khutbahTitle = obj.optString("khutbahTitle", ""),
                time = obj.optString("time", "11:45 WIB (Adzan Pertama)")
            )
        } catch (e: Exception) {
            getInitialFridaySchedule()
        }
    }

    private fun saveFridaySchedule(friday: FridaySchedule) {
        try {
            val obj = org.json.JSONObject()
            obj.put("id", friday.id)
            obj.put("date", friday.date)
            obj.put("khatib", friday.khatib)
            obj.put("imam", friday.imam)
            obj.put("muadzin", friday.muadzin)
            obj.put("bilal", friday.bilal)
            obj.put("khutbahTitle", friday.khutbahTitle)
            obj.put("time", friday.time)
            prefs.edit().putString("saved_friday_schedule", obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadEidSchedules(): List<EidSchedule> {
        val jsonStr = prefs.getString("saved_eid_schedules", null) ?: return getInitialEidSchedules()
        return try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<EidSchedule>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    EidSchedule(
                        id = obj.getString("id"),
                        eidType = obj.getString("eidType"),
                        date = obj.getString("date"),
                        time = obj.optString("time", "06:30 WIB"),
                        khatib = obj.getString("khatib"),
                        imam = obj.getString("imam"),
                        bilal = obj.getString("bilal"),
                        location = obj.optString("location", "Ruang Utama & Pelataran Masjid Jami' At-Taqwa"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            if (list.isNotEmpty()) list else getInitialEidSchedules()
        } catch (e: Exception) {
            getInitialEidSchedules()
        }
    }

    private fun saveEidSchedules(list: List<EidSchedule>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("eidType", item.eidType)
                obj.put("date", item.date)
                obj.put("time", item.time)
                obj.put("khatib", item.khatib)
                obj.put("imam", item.imam)
                obj.put("bilal", item.bilal)
                obj.put("location", item.location)
                obj.put("notes", item.notes)
                array.put(obj)
            }
            prefs.edit().putString("saved_eid_schedules", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadIqomahDurations(): Map<String, Int> {
        val defaults = mapOf(
            "Subuh" to 12,
            "Dzuhur" to 10,
            "Ashar" to 10,
            "Maghrib" to 8,
            "Isya" to 10
        )
        val jsonStr = prefs.getString("saved_iqomah_durations", null) ?: return defaults
        return try {
            val obj = org.json.JSONObject(jsonStr)
            val map = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getInt(key)
            }
            if (map.isNotEmpty()) map else defaults
        } catch (e: Exception) {
            defaults
        }
    }

    private fun saveIqomahDurations(map: Map<String, Int>) {
        try {
            val obj = org.json.JSONObject()
            map.forEach { (k, v) -> obj.put(k, v) }
            prefs.edit().putString("saved_iqomah_durations", obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
