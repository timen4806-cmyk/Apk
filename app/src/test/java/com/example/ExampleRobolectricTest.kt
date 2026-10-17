package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Jami' At-Taqwa", appName)
    }

    @Test
    fun `calculate prayer times for Penancangan Serang`() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 17, 12, 0, 0)
        }
        val schedule = PrayerTimeCalculator.calculateTimes(calendar)
        assertNotNull(schedule.subuh)
        assertNotNull(schedule.dzuhur)
        assertNotNull(schedule.ashar)
        assertNotNull(schedule.maghrib)
        assertNotNull(schedule.isya)
        assertTrue(schedule.subuh.contains(":"))
        assertTrue(schedule.maghrib.contains(":"))
    }
}
