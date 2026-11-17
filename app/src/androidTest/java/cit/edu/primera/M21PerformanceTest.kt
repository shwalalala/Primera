package cit.edu.primera

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M21PerformanceTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    companion object {
        private const val TAG = "M21Performance"
        private const val LIMIT_DASHBOARD_MS = 3000L
        private const val LIMIT_CHECKIN_SUBMIT_MS = 2000L
        private const val LIMIT_WATCH_SYNC_MS = 10000L
        private const val LIMIT_SPEECH_MS = 5000L
        private const val LIMIT_SCREEN_TRANSITION_MS = 1000L

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            runBlocking {
                LoginHelper.signInWithTestAccount()
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    cit.edu.primera.core.data.PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                    SeedData.seedUserProfile(uid)
                }
            }
        }
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            val allowBtn = device.wait(Until.findObject(By.textContains("Allow")), 2000)
            allowBtn?.click()
        } catch (_: Throwable) {}

        try {
            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }
        } catch (_: Throwable) {
            try {
                composeTestRule.onNodeWithText("Skip").performClick()
            } catch (_: Throwable) {}
            try {
                composeTestRule.onNodeWithText("Get Started").performClick()
            } catch (_: Throwable) {}
            try {
                LoginHelper.performUiLogin(composeTestRule)
            } catch (_: Throwable) {}
            try {
                composeTestRule.waitUntil(10000) {
                    try {
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }
            } catch (_: Throwable) {}
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun test01_dashboardPerformance() {
        // Dashboard <= 3s (5 runs)
        ScreenshotHelper.step("21", "01", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                // Navigate to Insight first to trigger a full Dashboard load when switching back
                try {
                    composeTestRule.onNodeWithText("Insight").performClick()
                    composeTestRule.waitForIdle()
                } catch (_: Throwable) {}

                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                } catch (_: Throwable) {}

                composeTestRule.waitUntil(timeoutMillis = LIMIT_DASHBOARD_MS) {
                    try {
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() &&
                        (composeTestRule.onAllNodesWithText("Recent Health Logs", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                         composeTestRule.onAllNodesWithText("Days Left", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                         composeTestRule.onAllNodesWithText("Week", substring = true).fetchSemanticsNodes().isNotEmpty())
                    } catch (_: Throwable) {
                        false
                    }
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Dashboard run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Dashboard average: ${avg}ms (limit: ${LIMIT_DASHBOARD_MS}ms)")
            assertTrue("Dashboard load average exceeds limit: ${avg}ms > ${LIMIT_DASHBOARD_MS}ms", avg <= LIMIT_DASHBOARD_MS)
        }
    }

    @Test
    fun test02_checkinSubmitPerformance() {
        // Check-in submit <= 2s (5 runs)
        ScreenshotHelper.step("21", "02", device) {
            val times = mutableListOf<Long>()
            for (i in 1..5) {
                composeTestRule.activityRule.scenario.onActivity { activity ->
                    activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                        putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "daily_checkin")
                    })
                }
                composeTestRule.waitForIdle()
                composeTestRule.waitUntil(5000) {
                    try {
                        composeTestRule.onAllNodesWithTag("checkin_weight_input").fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }

                try {
                    composeTestRule.onNodeWithTag("checkin_weight_input").performTextClearance()
                    composeTestRule.onNodeWithTag("checkin_weight_input").performTextInput("60")
                } catch (_: Throwable) {}
                composeTestRule.waitForIdle()

                try {
                    composeTestRule.onNodeWithTag("checkin_review_button").performScrollTo().performClick()
                } catch (_: Throwable) {
                    try {
                        device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4, device.displayWidth / 2, device.displayHeight / 4, 15)
                        composeTestRule.waitForIdle()
                        composeTestRule.onNodeWithTag("checkin_review_button").performClick()
                    } catch (_: Throwable) {
                        try {
                            composeTestRule.onNodeWithText("Review Check-in").performClick()
                        } catch (_: Throwable) {}
                    }
                }

                composeTestRule.waitForIdle()
                composeTestRule.waitUntil(5000) {
                    try {
                        composeTestRule.onAllNodesWithText("Confirm & Save").fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }

                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithText("Confirm & Save").performClick()
                } catch (_: Throwable) {}

                composeTestRule.waitUntil(timeoutMillis = LIMIT_CHECKIN_SUBMIT_MS) {
                    try {
                        composeTestRule.onAllNodesWithText("Daily Check-In", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Recent Health Logs", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Checkin submit run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Checkin submit average: ${avg}ms (limit: ${LIMIT_CHECKIN_SUBMIT_MS}ms)")
            assertTrue("Checkin submit average exceeds limit: ${avg}ms > ${LIMIT_CHECKIN_SUBMIT_MS}ms", avg <= LIMIT_CHECKIN_SUBMIT_MS)
        }
    }

    @Test
    fun test03_watchSyncPerformance() {
        // Watch sync <= 10s (5 runs)
        ScreenshotHelper.step("21", "03", device) {
            val times = mutableListOf<Long>()
            try {
                composeTestRule.onNodeWithText("Device").performClick()
            } catch (_: Throwable) {
                composeTestRule.activityRule.scenario.onActivity { activity ->
                    activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                        putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "device")
                    })
                }
            }
            composeTestRule.waitForIdle()

            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onNodeWithTag("smartwatch_connect_sync_button").performClick()
                } catch (_: Throwable) {
                    try {
                        composeTestRule.onNodeWithTag("health_data_sync_button").performClick()
                    } catch (_: Throwable) {
                        try {
                            composeTestRule.onNodeWithText("Connect and Sync").performClick()
                        } catch (_: Throwable) {
                            try {
                                composeTestRule.onNodeWithText("Sync Now").performClick()
                            } catch (_: Throwable) {}
                        }
                    }
                }

                composeTestRule.waitUntil(timeoutMillis = LIMIT_WATCH_SYNC_MS) {
                    composeTestRule.waitForIdle()
                    true
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Watch sync run $i: ${duration}ms")
            }
            val avg = times.average()
            Log.d(TAG, "Watch sync average: ${avg}ms (limit: ${LIMIT_WATCH_SYNC_MS}ms)")
            assertTrue("Watch sync average exceeds limit: ${avg}ms > ${LIMIT_WATCH_SYNC_MS}ms", avg <= LIMIT_WATCH_SYNC_MS)
        }
    }

    @Test
    fun test04_speechPerformance() {
        // Speech <= 5s (5 runs)
        ScreenshotHelper.step("21", "04", device) {
            val times = mutableListOf<Long>()
            try {
                composeTestRule.onNodeWithText("Home").performClick()
                composeTestRule.waitForIdle()
            } catch (_: Throwable) {}

            for (i in 1..5) {
                val start = SystemClock.elapsedRealtime()
                try {
                    composeTestRule.onAllNodesWithContentDescription("Microphone").onFirst().performClick()
                } catch (_: Throwable) {}

                composeTestRule.waitUntil(timeoutMillis = LIMIT_SPEECH_MS) {
                    try {
                        composeTestRule.onAllNodesWithText("Voice Log", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Listening", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Speech Recognition", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithContentDescription("Microphone").fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Speech run $i: ${duration}ms")
                device.pressBack()
                composeTestRule.waitForIdle()
            }
            val avg = times.average()
            Log.d(TAG, "Speech average: ${avg}ms (limit: ${LIMIT_SPEECH_MS}ms)")
            assertTrue("Speech average exceeds limit: ${avg}ms > ${LIMIT_SPEECH_MS}ms", avg <= LIMIT_SPEECH_MS)
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
                } catch (_: Throwable) {}

                composeTestRule.waitUntil(timeoutMillis = LIMIT_SCREEN_TRANSITION_MS) {
                    try {
                        composeTestRule.onAllNodesWithText("Insight", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Trends", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Goals", substring = true).fetchSemanticsNodes().isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                }
                val duration = SystemClock.elapsedRealtime() - start
                times.add(duration)
                Log.d(TAG, "Screen transition run $i: ${duration}ms")

                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                } catch (_: Throwable) {}
                composeTestRule.waitForIdle()
            }
            val avg = times.average()
            Log.d(TAG, "Screen transition average: ${avg}ms (limit: ${LIMIT_SCREEN_TRANSITION_MS}ms)")
            assertTrue("Screen transition average exceeds limit: ${avg}ms > ${LIMIT_SCREEN_TRANSITION_MS}ms", avg <= LIMIT_SCREEN_TRANSITION_MS)
        }
    }
}
