package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MosqueRepository
import com.example.data.util.NotificationHelper
import com.example.data.util.PrayerTimeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MosqueRepository(application)

    // Current Time & Clock
    private val _currentTime = MutableStateFlow("00:00:00")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDateMasehi = MutableStateFlow("")
    val currentDateMasehi: StateFlow<String> = _currentDateMasehi.asStateFlow()

    private val _currentDateHijri = MutableStateFlow("")
    val currentDateHijri: StateFlow<String> = _currentDateHijri.asStateFlow()

    // Prayer Schedule (Kemenag RI)
    private val _prayerSchedule = MutableStateFlow(PrayerTimeCalculator.calculateTimes())
    val prayerSchedule: StateFlow<PrayerDaySchedule> = _prayerSchedule.asStateFlow()

    // Next Prayer & Countdown
    private val _nextPrayerName = MutableStateFlow("Maghrib")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("00:00:00")
    val nextPrayerCountdown: StateFlow<String> = _nextPrayerCountdown.asStateFlow()

    // Iqomah Mode
    private val _isIqomahCountingDown = MutableStateFlow(false)
    val isIqomahCountingDown: StateFlow<Boolean> = _isIqomahCountingDown.asStateFlow()

    private val _iqomahCountdownText = MutableStateFlow("")
    val iqomahCountdownText: StateFlow<String> = _iqomahCountdownText.asStateFlow()

    private val _iqomahPrayerName = MutableStateFlow("")
    val iqomahPrayerName: StateFlow<String> = _iqomahPrayerName.asStateFlow()

    // TV Display Mode (Fullscreen Signage Mode for Smart TV / Projector / Monitor)
    private val _isTvMode = MutableStateFlow(false)
    val isTvMode: StateFlow<Boolean> = _isTvMode.asStateFlow()

    // Dark Mode Toggle
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Force Landscape orientation toggle
    private val _isLandscapeForced = MutableStateFlow(false)
    val isLandscapeForced: StateFlow<Boolean> = _isLandscapeForced.asStateFlow()

    fun toggleForceLandscape() {
        _isLandscapeForced.value = !_isLandscapeForced.value
    }

    // UI Tab Selection (0: Beranda, 1: Petugas, 2: Agenda & Galeri, 3: Kas & Donasi, 4: Admin)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Data from Repository
    val dailyOfficers: StateFlow<List<DailyPrayerOfficer>> = repository.dailyOfficers
    val fridaySchedule: StateFlow<FridaySchedule> = repository.fridaySchedule
    val eidSchedules: StateFlow<List<EidSchedule>> = repository.eidSchedules
    val announcements: StateFlow<List<Announcement>> = repository.announcements
    val kasTransactions: StateFlow<List<KasItem>> = repository.kasTransactions
    val agendas: StateFlow<List<AgendaItem>> = repository.agendas
    val galleryItems: StateFlow<List<GalleryItem>> = repository.galleryItems
    val isCloudSynced: StateFlow<Boolean> = repository.isCloudSynced
    val lastSyncTime: StateFlow<String> = repository.lastSyncTime

    init {
        NotificationHelper.createNotificationChannels(application)
        startClockTicker()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
            while (true) {
                val now = Calendar.getInstance()
                val timeString = timeSdf.format(now.time)
                _currentTime.value = timeString

                // Update schedule if needed
                val schedule = PrayerTimeCalculator.calculateTimes(now)
                _prayerSchedule.value = schedule
                _currentDateMasehi.value = schedule.dateMasehi
                _currentDateHijri.value = schedule.dateHijri

                // Update Next Prayer & Iqomah Countdown
                updateNextPrayerAndIqomah(now, schedule)

                delay(1000L)
            }
        }
    }

    private fun updateNextPrayerAndIqomah(now: Calendar, schedule: PrayerDaySchedule) {
        val currentMinutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        val prayers = listOf(
            Triple("Subuh", parseTimeToMinutes(schedule.subuh), 12),
            Triple("Dzuhur", parseTimeToMinutes(schedule.dzuhur), 10),
            Triple("Ashar", parseTimeToMinutes(schedule.ashar), 10),
            Triple("Maghrib", parseTimeToMinutes(schedule.maghrib), 8),
            Triple("Isya", parseTimeToMinutes(schedule.isya), 10)
        )

        // Check if currently inside Iqomah waiting window
        var insideIqomah = false
        for ((name, prayerMin, iqomahWait) in prayers) {
            val iqomahEndMin = prayerMin + iqomahWait
            if (currentMinutesOfDay >= prayerMin && currentMinutesOfDay < iqomahEndMin) {
                insideIqomah = true
                val remainingSec = (iqomahEndMin * 60) - (currentMinutesOfDay * 60 + currentSeconds)
                val remMin = remainingSec / 60
                val remSec = remainingSec % 60
                _isIqomahCountingDown.value = true
                _iqomahPrayerName.value = name
                _iqomahCountdownText.value = String.format(Locale.ROOT, "%02d:%02d", remMin, remSec)
                break
            }
        }

        if (!insideIqomah) {
            _isIqomahCountingDown.value = false
            _iqomahCountdownText.value = ""
        }

        // Find next prayer
        var nextPrayer: Triple<String, Int, Int>? = null
        for (prayer in prayers) {
            if (prayer.second > currentMinutesOfDay) {
                nextPrayer = prayer
                break
            }
        }

        if (nextPrayer == null) {
            // Next prayer is tomorrow Subuh
            val firstPrayer = prayers.first()
            val totalSecondsUntilMidnight = (24 * 60 * 60) - (currentMinutesOfDay * 60 + currentSeconds)
            val totalSeconds = totalSecondsUntilMidnight + (firstPrayer.second * 60)
            _nextPrayerName.value = firstPrayer.first
            _nextPrayerCountdown.value = formatSecondsToCountdown(totalSeconds)
        } else {
            val remainingSeconds = (nextPrayer.second * 60) - (currentMinutesOfDay * 60 + currentSeconds)
            _nextPrayerName.value = nextPrayer.first
            _nextPrayerCountdown.value = formatSecondsToCountdown(remainingSeconds)
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun formatSecondsToCountdown(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.ROOT, "%02d:%02d:%02d", h, m, s)
    }

    // Toggle TV / Digital Signage mode
    fun setTvMode(enabled: Boolean) {
        _isTvMode.value = enabled
    }

    // Toggle Dark Theme
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    // Select Main Tab
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // Admin updates
    fun updateDailyOfficer(officer: DailyPrayerOfficer) {
        repository.updateDailyOfficer(officer)
    }

    fun updateFridaySchedule(friday: FridaySchedule) {
        repository.updateFridaySchedule(friday)
    }

    fun updateEidSchedule(eid: EidSchedule) {
        repository.updateEidSchedule(eid)
    }

    fun addAnnouncement(text: String) {
        repository.addAnnouncement(text)
    }

    fun toggleAnnouncement(id: String) {
        repository.toggleAnnouncement(id)
    }

    fun deleteAnnouncement(id: String) {
        repository.deleteAnnouncement(id)
    }

    fun addKasTransaction(title: String, amount: Long, type: TransactionType, category: String, notes: String = "") {
        repository.addKasTransaction(title, amount, type, category, notes)
    }

    fun deleteKasTransaction(id: String) {
        repository.deleteKasTransaction(id)
    }

    fun addAgenda(title: String, speaker: String, date: String, time: String, location: String, description: String, category: String) {
        repository.addAgenda(title, speaker, date, time, location, description, category)
    }

    fun deleteAgenda(id: String) {
        repository.deleteAgenda(id)
    }

    fun addGalleryItem(title: String, category: String, description: String) {
        repository.addGalleryItem(title, category, description)
    }

    fun triggerCloudSync() {
        repository.triggerCloudSync()
    }

    // Push notification triggers
    fun sendBroadcastNotification(title: String, message: String) {
        NotificationHelper.showBroadcastNotification(getApplication(), title, message)
        repository.addAnnouncement("📢 SIARAN: $message")
    }

    fun triggerPrayerReminderNow() {
        NotificationHelper.showPrayerReminder(getApplication(), _nextPrayerName.value, _currentTime.value.substring(0, 5))
    }

    fun triggerIqomahReminderNow() {
        NotificationHelper.showIqomahReminder(getApplication(), _nextPrayerName.value, 10)
    }

    fun triggerAgendaReminderNow(item: AgendaItem) {
        NotificationHelper.showAgendaReminder(getApplication(), item.title, "${item.date} (${item.time})", item.speaker)
    }
}
