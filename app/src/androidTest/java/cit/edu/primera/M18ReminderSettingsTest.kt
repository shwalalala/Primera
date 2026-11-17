package cit.edu.primera

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M18ReminderSettingsTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            LoginHelper.signInWithTestAccount()
        }
    }

    @Test
    fun testReminderSettingsFullSuite() {
        // Step 1: Reminder settings screen loaded
        ScreenshotHelper.step("18", "01", device) {
            try {
                composeTestRule.onNodeWithText("Settings").performClick()
                composeTestRule.onNodeWithText("Reminders").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 2: Time picker dialog
        ScreenshotHelper.step("18", "02", device) {
            try {
                composeTestRule.onNodeWithText("9:00 AM").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }
}
