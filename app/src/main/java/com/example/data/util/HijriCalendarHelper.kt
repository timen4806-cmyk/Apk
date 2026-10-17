package com.example.data.util

import java.util.Calendar
import kotlin.math.floor

object HijriCalendarHelper {

    private val HIJRI_MONTHS = arrayOf(
        "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir",
        "Jumadil Ula", "Jumadil Akhir", "Rajab", "Sya'ban",
        "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
    )

    fun getHijriDateString(calendar: Calendar = Calendar.getInstance()): String {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number
        var julian = d - 32075 +
                1461 * (y + 4800 + (m - 14) / 12) / 4 +
                367 * (m - 2 - (m - 14) / 12 * 12) / 12 -
                3 * ((y + 4900 + (m - 14) / 12) / 100) / 4

        // Convert JD to Hijri
        val l = julian - 1948440 + 10632
        val n = (l - 1) / 10631
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985 - lPrime) / 5316.0) * floor((50 * lPrime + 17719) / 17700.0) +
                floor(lPrime / 5670.0) * floor((43 * lPrime + 15238) / 15235.0)).toInt()
        val lDoublePrime = lPrime - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) +
                floor(j / 16.0) * floor((15235 * j) / 43.0)).toInt() + 29
        val hijriMonth = (24 * lDoublePrime / 709).toInt()
        val hijriDay = lDoublePrime - 709 * hijriMonth / 24
        val hijriYear = 30 * n + j - 30

        val validMonth = (hijriMonth - 1).coerceIn(0, 11)
        val monthName = HIJRI_MONTHS[validMonth]

        return "$hijriDay $monthName $hijriYear H"
    }
}
