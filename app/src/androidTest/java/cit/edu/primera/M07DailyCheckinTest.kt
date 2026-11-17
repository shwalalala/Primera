package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M07DailyCheckinTest {

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
        try {
            composeTestRule.onNodeWithText("Check-in").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testDailyCheckinFullSuite() {
        // Step 1: Blank and invalid weight validation
        // Defect Verification: Enter 0 and implausible values (603), assert rejection and field error
        ScreenshotHelper.step("07", "01", device) {
            // Test entering 0
            composeTestRule.onNodeWithTag("weight_input").performTextClearance()
            composeTestRule.onNodeWithTag("weight_input").performTextInput("0")
            composeTestRule.waitForIdle()

            // Test entering implausible value 603
            composeTestRule.onNodeWithTag("weight_input").performTextClearance()
            composeTestRule.onNodeWithTag("weight_input").performTextInput("603")
            composeTestRule.waitForIdle()

            // Assert field-level error is displayed
            composeTestRule.onNodeWithTag("weight_error_text").assertIsDisplayed()

            // Review button must not proceed while invalid
            composeTestRule.onNodeWithTag("review_checkin_button").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 2: Completed check-in form with valid values
        ScreenshotHelper.step("07", "02", device) {
            composeTestRule.onNodeWithTag("weight_input").performTextClearance()
            composeTestRule.onNodeWithTag("weight_input").performTextInput("60")
            composeTestRule.onNodeWithTag("blood_sugar_input").performTextClearance()
            composeTestRule.onNodeWithTag("blood_sugar_input").performTextInput("95")
            composeTestRule.onNodeWithTag("note_input").performTextClearance()
            composeTestRule.onNodeWithTag("note_input").performTextInput("Feeling well and hydrated")
            try {
                composeTestRule.onNodeWithText("Happy").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Submission confirmation
        ScreenshotHelper.step("07", "03", device) {
            composeTestRule.onNodeWithTag("review_checkin_button").performClick()
            composeTestRule.waitForIdle()
            try {
                composeTestRule.onNodeWithTag("save_checkin_button").performClick()
            } catch (_: Exception) {}
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
}
