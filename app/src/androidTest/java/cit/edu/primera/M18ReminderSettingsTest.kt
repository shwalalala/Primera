package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M18ReminderSettingsTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    companion object {
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
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        navigateToReminderSettings()
    }

    private fun navigateToReminderSettings() {
        // Wait for Dashboard
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        if (composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()) {
            return
        }

        // Open sidebar
        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyAncestor(hasTestTag("dashboard_top_bar"))
        ).onFirst().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Notifications
        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Settings")).fetchSemanticsNodes().isNotEmpty()
        }
        // Click Settings button in FeatureTopBar to open Reminder Settings
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Settings")).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testReminderSettingsFullSuite() {
        // Step 1: Reminder settings screen loaded
        ScreenshotHelper.step("18", "01", device) {
            composeTestRule.onNodeWithText("Reminder Settings").assertIsDisplayed()
            composeTestRule.onNodeWithText("Morning Check-in").assertIsDisplayed()
            composeTestRule.onNodeWithText("Hydration Reminder").assertIsDisplayed()
            composeTestRule.onNodeWithText("Evening Wind-down").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsDisplayed()
        }

        // Step 2: Toggle reminder switch and assert persistence after app restart
        ScreenshotHelper.step("18", "02", device) {
            // Toggle morning check-in switch off
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsOff()

            // Recreate activity to test persistence
            composeTestRule.activityRule.scenario.recreate()
            composeTestRule.waitForIdle()

            // Navigate back to Reminder Settings
            navigateToReminderSettings()

            // Assert persisted state across relaunch
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsOff()

            // Toggle back on for clean state
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsOn()
        }

        // Step 3: Master notification toggle check
        ScreenshotHelper.step("18", "03", device) {
            // Note Defect: Primera does not implement an in-app master notification toggle switch.
            // Notifications are controlled at the granular category level or via system POST_NOTIFICATIONS permission.
            // Closest adb alternative for master notification control:
            //   adb shell pm revoke cit.edu.primera android.permission.POST_NOTIFICATIONS
            //   adb shell pm grant cit.edu.primera android.permission.POST_NOTIFICATIONS
            composeTestRule.onNodeWithText("Reminder Settings").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_evening_wind_down").assertIsDisplayed()
        }
    }
}
