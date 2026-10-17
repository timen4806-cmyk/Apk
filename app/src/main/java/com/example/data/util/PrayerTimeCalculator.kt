package com.example.data.util

import com.example.data.model.PrayerDaySchedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

object PrayerTimeCalculator {

    // Default: Penancangan, Cipocok Jaya / Serang, Banten (WIB)
    const val DEFAULT_LAT = -6.1104
    const val DEFAULT_LNG = 106.1640
    const val DEFAULT_TIMEZONE = 7.0

    // Kemenag RI calculation angles
    private const val SUBUH_ANGLE = -20.0
    private const val ISYA_ANGLE = -18.0
    private const val TERBIT_ANGLE = -0.8333
    private const val DHUHA_ANGLE = 4.5
    private const val MAGHRIB_ANGLE = -1.0
    private const val IHTIYAT_MINUTES = 2.0 // standard Kemenag safety margin

    fun calculateTimes(
        calendar: Calendar = Calendar.getInstance(),
        lat: Double = DEFAULT_LAT,
        lng: Double = DEFAULT_LNG,
        tz: Double = DEFAULT_TIMEZONE,
        cityName: String = "Penancangan, Serang (Banten)"
    ): PrayerDaySchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day Calculation
        val a = floor((14 - month) / 12.0)
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jd = day + floor((153 * m + 2) / 5.0) + 365 * y + floor(y / 4.0) - floor(y / 100.0) + floor(y / 400.0) - 32045

        // Days since J2000.0
        val d = jd - 2451545.0

        // Sun coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val fixedRa = fixHour(ra)

        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = (q / 15.0) - fixedRa

        // Midday (Dzuhur transit)
        val transit = 12.0 + tz - (lng / 15.0) - eqTime

        // Calculations for each prayer
        val subuhHour = calcHourAngle(SUBUH_ANGLE, lat, declination)
        val terbitHour = calcHourAngle(TERBIT_ANGLE, lat, declination)
        val dhuhaHour = calcHourAngle(DHUHA_ANGLE, lat, declination)
        val maghribHour = calcHourAngle(MAGHRIB_ANGLE, lat, declination)
        val isyaHour = calcHourAngle(ISYA_ANGLE, lat, declination)

        // Ashar: Shafi'i shadow ratio = 1
        val asharAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(abs(lat - declination))))))
        val asharHour = calcHourAngle(asharAltitude, lat, declination)

        // Decimal hours with Ihtiyat (+2 minutes)
        val dzuhurDecimal = transit + (IHTIYAT_MINUTES / 60.0)
        val subuhDecimal = transit - (subuhHour / 15.0) + (IHTIYAT_MINUTES / 60.0)
        val imsakDecimal = subuhDecimal - (10.0 / 60.0) // 10 minutes before subuh
        val terbitDecimal = transit - (terbitHour / 15.0) - (IHTIYAT_MINUTES / 60.0)
        val dhuhaDecimal = transit - (dhuhaHour / 15.0) + (IHTIYAT_MINUTES / 60.0)
        val asharDecimal = transit + (asharHour / 15.0) + (IHTIYAT_MINUTES / 60.0)
        val maghribDecimal = transit + (maghribHour / 15.0) + (IHTIYAT_MINUTES / 60.0)
        val isyaDecimal = transit + (isyaHour / 15.0) + (IHTIYAT_MINUTES / 60.0)

        val masehiSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val dateMasehi = masehiSdf.format(calendar.time)
        val dateHijri = HijriCalendarHelper.getHijriDateString(calendar)

        return PrayerDaySchedule(
            imsak = formatHour(imsakDecimal),
            subuh = formatHour(subuhDecimal),
            terbit = formatHour(terbitDecimal),
            dhuha = formatHour(dhuhaDecimal),
            dzuhur = formatHour(dzuhurDecimal),
            ashar = formatHour(asharDecimal),
            maghrib = formatHour(maghribDecimal),
            isya = formatHour(isyaDecimal),
            dateMasehi = dateMasehi,
            dateHijri = dateHijri,
            cityName = cityName
        )
    }

    private fun calcHourAngle(angle: Double, lat: Double, declination: Double): Double {
        val cosHour = (sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
                (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))
        val clamped = cosHour.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h - 24.0 * floor(h / 24.0)
        if (res < 0) res += 24.0
        return res
    }

    private fun formatHour(dec: Double): String {
        var h = floor(dec).toInt()
        val m = floor((dec - h) * 60.0 + 0.5).toInt()
        if (m >= 60) {
            h += 1
        }
        val cleanH = (h % 24)
        val cleanM = (m % 60)
        return String.format(Locale.ROOT, "%02d:%02d", cleanH, cleanM)
    }
}
