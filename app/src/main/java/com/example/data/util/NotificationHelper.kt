package com.example.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_PRAYER_ID = "channel_prayer_iqomah"
    private const val CHANNEL_BROADCAST_ID = "channel_broadcast_announcements"
    private const val CHANNEL_AGENDA_ID = "channel_agenda_kajian"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "Jadwal Shalat & Iqomah",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi waktu shalat dan pengingat jeda iqomah"
                enableVibration(true)
            }

            val broadcastChannel = NotificationChannel(
                CHANNEL_BROADCAST_ID,
                "Pesan Siaran Pengurus",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengumuman dan siaran resmi DKM Masjid Jami' At-Taqwa"
                enableVibration(true)
            }

            val agendaChannel = NotificationChannel(
                CHANNEL_AGENDA_ID,
                "Agenda & Pengajian",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat jadwal kajian rutin dan agenda keagamaan"
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(broadcastChannel)
            notificationManager.createNotificationChannel(agendaChannel)
        }
    }

    fun showBroadcastNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BROADCAST_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📢 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showPrayerReminder(context: Context, prayerName: String, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 Waktu Shalat $prayerName Telah Tiba")
            .setContentText("Pukul $time WIB - Masjid Jami' At-Taqwa Penancangan. Mari shalat berjamaah!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(2001, notification)
    }

    fun showIqomahReminder(context: Context, prayerName: String, remainingMinutes: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏳ Pengingat Iqomah $prayerName")
            .setContentText("Iqomah $prayerName akan dikumandangkan dalam $remainingMinutes menit lagi. Harap bersiap di shaf.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(2002, notification)
    }

    fun showAgendaReminder(context: Context, agendaTitle: String, timeInfo: String, speaker: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_AGENDA_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("📖 Pengingat Kajian: $agendaTitle")
            .setContentText("$timeInfo bersama $speaker di Masjid Jami' At-Taqwa")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$timeInfo bersama $speaker di Masjid Jami' At-Taqwa. Hadirilah bersama keluarga."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(3001, notification)
    }
}
