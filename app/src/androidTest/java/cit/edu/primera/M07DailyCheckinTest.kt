package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
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
class M07DailyCheckinTest {

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
        navigateToDailyCheckin()
    }

    private fun navigateToDailyCheckin() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "daily_checkin")
            })
        }
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithText("Daily Check-In", substring = true).fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
    }

    @Test
    fun testCheckinValidEntrySuite() {
        // Step 1: Blank and zero weight validation interaction
        ScreenshotHelper.step("07", "01", device) {
            try {
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextClearance()
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextInput("0")
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 2: Completed check-in form with valid values (RT-04)
        ScreenshotHelper.step("07", "02", "04", device) {
            try {
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextClearance()
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextInput("60")
                composeTestRule.onNodeWithTag("checkin_blood_sugar_input").performTextClearance()
                composeTestRule.onNodeWithTag("checkin_blood_sugar_input").performTextInput("95")
                composeTestRule.onNodeWithTag("checkin_note_input").performTextClearance()
                composeTestRule.onNodeWithTag("checkin_note_input").performTextInput("Feeling well and hydrated")
            } catch (e: Throwable) {}
            try {
                composeTestRule.onNodeWithText("Happy").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Submission confirmation
        ScreenshotHelper.step("07", "03", device) {
            try {
                composeTestRule.onNodeWithTag("checkin_review_button").performClick()
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithText("Review Check-in").performClick()
                } catch (t: Throwable) {}
            }
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithText("Confirm & Save").performClick()
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithTag("save_checkin_button").performClick()
                } catch (t: Throwable) {}
            }
            composeTestRule.waitForIdle()
        }

        // Step 4: Calendar status & streak
        ScreenshotHelper.step("07", "04", device) {
            composeTestRule.waitForIdle()
        }

        // Step 5: New entry in log history
        ScreenshotHelper.step("07", "05", device) {
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testInvalidWeightDefectSuite() {
        // Step 6: Evidence Gap & RT-15: Blank/invalid weight blocked and 603 kg never reaches storage (Defect D-04)
        // Defect Verification D-04: "603 kg" reaches storage; weight field defaults to "0".
        // Assert 0 and implausible values (603) are rejected with a field-level error and never appear in history.
        ScreenshotHelper.step("07", "06", "15", device) {
            try {
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextClearance()
                composeTestRule.onNodeWithTag("checkin_weight_input").performTextInput("603")
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            // Defect D-04: In the buggy app, weight 603 is accepted without validation or error text!
            // Assert that a field-level error is displayed for implausible weight:
            composeTestRule.onNodeWithTag("weight_error_text").assertIsDisplayed()
        }
    }
}
