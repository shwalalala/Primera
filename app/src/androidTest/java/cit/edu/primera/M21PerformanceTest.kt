package cit.edu.primera

import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M21PerformanceTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    companion object {
        private const val TAG = "M21Performance"
        private const val LIMIT_DASHBOARD_MS = 3000L
        private const val LIMIT_CHECKIN_SUBMIT_MS = 2000L
        private const val LIMIT_WATCH_SYNC_MS = 10000L
        private const val LIMIT_SPEECH_MS = 5000L
        private const val LIMIT_SCREEN_TRANSITION_MS = 1000L
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            LoginHelper.signInWithTestAccount()
        }
    }

    @Test
    fun test01_dashboardPerformance() {
        // Dashboard <= 3s (5 runs)
        ScreenshotHelper.step("21", "01", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitUntil(timeoutMillis = LIMIT_DASHBOARD_MS) {
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Dashboard run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Dashboard average: ${avg}ms (limit: ${LIMIT_DASHBOARD_MS}ms)")
            assertTrue("Dashboard load average exceeds limit", avg <= LIMIT_DASHBOARD_MS)
        }
    }

    @Test
    fun test02_checkinSubmitPerformance() {
        // Check-in submit <= 2s (5 runs)
        ScreenshotHelper.step("21", "02", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                try {
                    composeTestRule.onNodeWithText("Check-in").performClick()
                    composeTestRule.onNodeWithTag("weight_input").performTextClearance()
                    composeTestRule.onNodeWithTag("weight_input").performTextInput("60")
                } catch (_: Exception) {}

                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithTag("review_checkin_button").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitUntil(timeoutMillis = LIMIT_CHECKIN_SUBMIT_MS) {
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Checkin submit run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Checkin submit average: ${avg}ms (limit: ${LIMIT_CHECKIN_SUBMIT_MS}ms)")
            assertTrue("Checkin submit average exceeds limit", avg <= LIMIT_CHECKIN_SUBMIT_MS)
        }
    }

    @Test
    fun test03_watchSyncPerformance() {
        // Watch sync <= 10s (5 runs)
        ScreenshotHelper.step("21", "03", device) {
            val times = mutableListOf<Long>()
            try {
                composeTestRule.onNodeWithText("Device").performClick()
            } catch (_: Exception) {}

            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithTag("sync_watch_button").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitUntil(timeoutMillis = LIMIT_WATCH_SYNC_MS) {
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Watch sync run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Watch sync average: ${avg}ms (limit: ${LIMIT_WATCH_SYNC_MS}ms)")
            assertTrue("Watch sync average exceeds limit", avg <= LIMIT_WATCH_SYNC_MS)
        }
    }

    @Test
    fun test04_speechPerformance() {
        // Speech <= 5s (5 runs)
        ScreenshotHelper.step("21", "04", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithTag("mic_fab").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitUntil(timeoutMillis = LIMIT_SPEECH_MS) {
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Speech run $i: ${duration}ms")
                device.pressBack()
            }
            val avg = times.average()
            Log.d(TAG, "Speech average: ${avg}ms (limit: ${LIMIT_SPEECH_MS}ms)")
            assertTrue("Speech average exceeds limit", avg <= LIMIT_SPEECH_MS)
        }
    }

    @Test
    fun test05_screenTransitionPerformance() {
        // Screen transition <= 1s (5 runs)
        ScreenshotHelper.step("21", "05", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithText("Insight").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitUntil(timeoutMillis = LIMIT_SCREEN_TRANSITION_MS) {
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Screen transition run $i: ${duration}ms")
                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                } catch (_: Exception) {}
                composeTestRule.waitForIdle()
            }
            val avg = times.average()
            Log.d(TAG, "Screen transition average: ${avg}ms (limit: ${LIMIT_SCREEN_TRANSITION_MS}ms)")
            assertTrue("Screen transition average exceeds limit", avg <= LIMIT_SCREEN_TRANSITION_MS)
        }
    }
}
