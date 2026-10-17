package com.example.data.util

import java.util.Calendar

object HijriCalendarHelper {

    private val HIJRI_MONTHS = arrayOf(
        "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir",
        "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
        "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
    )

    // Standard 30-year leap cycle (years 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
    private val LEAP_YEARS = setOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)

    /**
     * Converts a Gregorian Calendar date into Hijri date (Day, Month [1..12], Year).
     * @param calendar Gregorian calendar instance
     * @param dayOffset optional adjustment offset in days (-2..+2) for local ru'yatul hilal alignment
     */
    fun getHijriDate(calendar: Calendar = Calendar.getInstance(), dayOffset: Int = 0): Triple<Int, Int, Int> {
        var y = calendar.get(Calendar.YEAR)
        var m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        val jd = (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + d + b - 1524

        // Civil Islamic calendar epoch: July 16, 622 CE -> JD 1948440
        val days = (jd - 1948440L) + dayOffset
        val cycle = days / 10631L
        val remDays = (days % 10631L).toInt()

        var yearInCycle = 0
        var daysAcc = 0
        while (yearInCycle < 30) {
            val yearLen = if (LEAP_YEARS.contains(yearInCycle + 1)) 355 else 354
            if (remDays < daysAcc + yearLen) {
                break
            }
            daysAcc += yearLen
            yearInCycle++
        }

        val hijriYear = (cycle * 30 + yearInCycle + 1).toInt()
        val dayInYear = remDays - daysAcc

        val monthDays = intArrayOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)
        if (LEAP_YEARS.contains(yearInCycle + 1)) {
            monthDays[11] = 30
        }

        var hijriMonth = 1
        var hijriDay = 1
        var daysInMAcc = 0
        for (mIdx in monthDays.indices) {
            val mLen = monthDays[mIdx]
            if (dayInYear < daysInMAcc + mLen) {
                hijriMonth = mIdx + 1
                hijriDay = dayInYear - daysInMAcc + 1
                break
            }
            daysInMAcc += mLen
        }

        return Triple(hijriDay, hijriMonth, hijriYear)
    }

    fun getHijriDateString(calendar: Calendar = Calendar.getInstance(), dayOffset: Int = 0): String {
        val (day, month, year) = getHijriDate(calendar, dayOffset)
        val validMonth = (month - 1).coerceIn(0, 11)
        val monthName = HIJRI_MONTHS[validMonth]
        return "$day $monthName $year H"
    }
}
