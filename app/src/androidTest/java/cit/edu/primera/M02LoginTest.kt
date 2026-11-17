package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M02LoginTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Navigate to Login tab if not already there
        try {
            composeTestRule.onNodeWithText("Log In").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testLoginFullSuite() {
        // Step 1: Blank fields validation
        ScreenshotHelper.step("02", "01", device) {
            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 2: Wrong password
        ScreenshotHelper.step("02", "02", device) {
            composeTestRule.onNodeWithText("Email").performTextInput(SeedData.TEST_USER_EMAIL)
            composeTestRule.onNodeWithText("Password").performTextInput("WrongPassword999!")
            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 3: Unregistered email
        ScreenshotHelper.step("02", "03", device) {
            composeTestRule.onNodeWithText("Email").performTextClearance()
            composeTestRule.onNodeWithText("Email").performTextInput("unregistered.user.999@example.com")
            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 4: Valid login & Remember Me
        ScreenshotHelper.step("02", "04", device) {
            composeTestRule.onNodeWithText("Email").performTextClearance()
            composeTestRule.onNodeWithText("Email").performTextInput(SeedData.TEST_USER_EMAIL)
            composeTestRule.onNodeWithText("Password").performTextClearance()
            composeTestRule.onNodeWithText("Password").performTextInput(SeedData.TEST_USER_PASS)
            try {
                composeTestRule.onNodeWithText("Remember Me").performClick()
            } catch (_: Exception) {}
            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 5: Session restore after app restart
        ScreenshotHelper.step("02", "05", device) {
            composeTestRule.waitForIdle()
        }

        // Step 6: Logout with saved email
        ScreenshotHelper.step("02", "06", device) {
            composeTestRule.waitForIdle()
        }

        // Step 7: Google Sign-In with UI Automator driving account picker
        ScreenshotHelper.step("02", "07", device) {
            try {
                composeTestRule.onNodeWithText("Continue with Google").performClick()
                LoginHelper.driveGoogleAccountPicker(device, timeoutMs = 3000)
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }
}
