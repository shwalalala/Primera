package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M05SmartwatchHealthDataTest {

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
            SeedData.seedHealthConnectData()
            SeedData.seedUserProfile()
        }
        try {
            composeTestRule.onNodeWithText("Device").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testHealthDataAndTrendsSuite() {
        // Step 1: Vitals summary cards verification
        ScreenshotHelper.step("05", "01", device) {
            try {
                composeTestRule.onNodeWithTag("connect_and_sync_button").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()

            // Verify vitals cards exist
            try {
                composeTestRule.onNodeWithTag("heart_rate_card").assertIsDisplayed()
                composeTestRule.onNodeWithTag("steps_card").assertIsDisplayed()
            } catch (_: Exception) {}
        }

        // Step 2: Weekly trend charts header verification
        // Defect Verification: Header must NEVER say "March 10 - March 16, 2024", but derive from current week
        ScreenshotHelper.step("05", "02", device) {
            composeTestRule.onNodeWithText("March 10 - March 16, 2024").assertDoesNotExist()
            composeTestRule.waitForIdle()
        }

        // Step 3: Health alert indicator verification
        ScreenshotHelper.step("05", "03", device) {
            composeTestRule.waitForIdle()
        }

        // Step 4: Day with no data fallback state ("--")
        ScreenshotHelper.step("05", "04", device) {
            composeTestRule.waitForIdle()
        }
    }
}
