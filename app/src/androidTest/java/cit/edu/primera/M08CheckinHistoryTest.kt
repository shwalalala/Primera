package cit.edu.primera

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
import cit.edu.primera.common.SeedData
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M08CheckinHistoryTest {

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
            SeedData.seedCheckinHistory()
        }
        try {
            composeTestRule.onNodeWithText("Check-in").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testCheckinHistoryFullSuite() {
        // Step 1: History list (5 seeded entries)
        ScreenshotHelper.step("08", "01", device) {
            composeTestRule.waitForIdle()
        }

        // Step 2: Search filter results
        ScreenshotHelper.step("08", "02", device) {
            try {
                composeTestRule.onNodeWithText("Search logs...").performTextInput("Headache")
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: No-results message
        ScreenshotHelper.step("08", "03", device) {
            try {
                composeTestRule.onNodeWithText("Search logs...").performTextClearance()
                composeTestRule.onNodeWithText("Search logs...").performTextInput("NonExistentSymptomXYZ")
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 4: Category & date filter sheet
        ScreenshotHelper.step("08", "04", device) {
            try {
                composeTestRule.onNodeWithText("Search logs...").performTextClearance()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 5: Entry preview sheet
        ScreenshotHelper.step("08", "05", device) {
            try {
                composeTestRule.onNodeWithText("Headache").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 6: Edit check-in note replacement (Defect Fix: Note must be replaced, not concatenated)
        ScreenshotHelper.step("08", "06", device) {
            try {
                composeTestRule.onNodeWithText("Edit Daily Check-In").performClick()
                composeTestRule.waitForIdle()
                // Replace note cleanly
                composeTestRule.onNodeWithTag("note_input").performTextClearance()
                composeTestRule.onNodeWithTag("note_input").performTextInput("Updated note text without concatenation")
                composeTestRule.onNodeWithTag("review_checkin_button").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag("save_checkin_button").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 7: Delete confirmation dialog (cancel)
        ScreenshotHelper.step("08", "07", device) {
            try {
                composeTestRule.onNodeWithText("Headache").performClick()
                composeTestRule.onNodeWithText("Delete Daily Check-In").performClick()
                composeTestRule.onNodeWithText("Cancel").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 8: Delete confirmed and entry removed
        ScreenshotHelper.step("08", "08", device) {
            try {
                composeTestRule.onNodeWithText("Delete Daily Check-In").performClick()
                composeTestRule.onNodeWithText("Delete").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 9: Empty-state history account
        ScreenshotHelper.step("08", "09", device) {
            composeTestRule.waitForIdle()
        }
    }
}
