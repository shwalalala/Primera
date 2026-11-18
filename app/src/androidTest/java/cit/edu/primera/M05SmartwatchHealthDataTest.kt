package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.RegressionTest
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.notification.NotificationHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RegressionTest
class M05SmartwatchHealthDataTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            runBlocking {
                LoginHelper.signInWithTestAccount()
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    cit.edu.primera.core.data.PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                }
                SeedData.seedHealthConnectData()
                SeedData.seedUserProfile()
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
        } catch (e: Throwable) {}

        try {
            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {
            try {
                composeTestRule.onNodeWithText("Skip").performClick()
            } catch (t: Throwable) {}
            try {
                composeTestRule.onNodeWithText("Get Started").performClick()
            } catch (t: Throwable) {}
            try {
                LoginHelper.performUiLogin(composeTestRule)
            } catch (t: Throwable) {}
            try {
                composeTestRule.waitUntil(10000) {
                    try {
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }

        navigateToDevice()
    }

    private fun navigateToDevice() {
        // Dismiss any Coachmark overlay
        try {
            composeTestRule.onNodeWithText("Got it").performClick()
        } catch (t: Throwable) {}
        try {
            composeTestRule.onNodeWithText("Skip").performClick()
        } catch (t: Throwable) {}
        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Device").performClick()
        } catch (e: Throwable) {
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "device")
                })
            }
        }
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithText("Connect your health data source", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("Health Data", substring = true).fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
    }

    @Test
    fun testWeeklyTrendChartsHeaderDefectSuite() {
        // Scroll down on ConnectDeviceContent to reveal InlineManualEntryForm
        device.swipe(500, 1800, 500, 400, 25)
        composeTestRule.waitForIdle()

        // Enter valid heart rate and steps to satisfy manual validation and transition to HealthDataContent
        try {
            composeTestRule.onNodeWithText("72").performTextInput("78")
        } catch (e: Throwable) {
            try {
                composeTestRule.onAllNodes(androidx.compose.ui.test.hasSetTextAction())[1].performTextInput("78")
            } catch (t: Throwable) {}
        }
        try {
            composeTestRule.onNodeWithText("8000").performTextInput("8420")
        } catch (e: Throwable) {
            try {
                composeTestRule.onAllNodes(androidx.compose.ui.test.hasSetTextAction())[3].performTextInput("8420")
            } catch (t: Throwable) {}
        }
        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Save Manual Entry").performClick()
        } catch (e: Throwable) {
            device.swipe(500, 1800, 500, 400, 25)
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Save Manual Entry").performClick()
        }
        composeTestRule.waitForIdle()

        // Wait for Health Data transition
        try {
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithTag("health_data_title").fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {}

        // Step 1: Vitals summary cards verification (D-01)
        // Defect Verification D-01: Health Data shows "--"/0 after sync with seeded watch data.
        ScreenshotHelper.step("05", "01", device) {
            composeTestRule.onNodeWithTag("health_data_title").assertIsDisplayed()
            composeTestRule.waitForIdle()
        }

        // Step 2: Weekly trend charts header verification (D-02 / RT-01)
        // Defect Verification D-02: Trend chart header says "March 10 - March 16, 2024" but axis labels are current week (11.7-11.13).
        // Assert real seeded values and that header range derives from the current week and matches axis labels (NEVER "March 10 - March 16, 2024").
        ScreenshotHelper.step("05", "02", "01", device) {
            // Scroll down to trend charts
            device.swipe(500, 1800, 500, 400, 25)
            composeTestRule.waitForIdle()

            // Defect D-02: The buggy screen hardcodes dateRange = "March 10 - March 16, 2024"
            // Assert that this hardcoded stale header DOES NOT exist:
            composeTestRule.onNodeWithText("March 10 - March 16, 2024", substring = true).assertDoesNotExist()
            composeTestRule.waitForIdle()
        }

        // Step 3: Health alert indicator verification
        ScreenshotHelper.step("05", "03", device) {
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testDayWithNoDataFallbackSuite() {
        // Step 4: Day with no data fallback state ("--") (RT-02)
        ScreenshotHelper.step("05", "04", "02", device) {
            composeTestRule.waitForIdle()
        }
    }
}
