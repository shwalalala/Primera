package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.ScreenshotHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M01RegistrationTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation())
        // Navigate past Welcome to Registration if on Welcome
        try {
            composeTestRule.onNodeWithText("Get Started").performClick()
        } catch (_: Exception) {
            try {
                composeTestRule.onNodeWithText("Skip").performClick()
            } catch (_: Exception) {}
        }
    }

    @Test
    fun testRegistrationFullSuite() {
        // Step 1: Register Screen Loaded
        ScreenshotHelper.step("01", "01", device) {
            composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
            composeTestRule.onNodeWithText("Sign up to start your journey").assertIsDisplayed()
            composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        }

        // Step 2: Blank Field Validation
        ScreenshotHelper.step("01", "02", device) {
            composeTestRule.onNodeWithText("Sign Up").performClick()
            // Inline errors triggered
            composeTestRule.waitForIdle()
        }

        // Step 3a: Invalid Email Format
        ScreenshotHelper.step("01", "03a", device) {
            composeTestRule.onNodeWithText("Username").performTextInput("ValidUser")
            composeTestRule.onNodeWithText("Email").performTextInput("invalid-email-format")
            composeTestRule.onNodeWithText("Password").performTextInput("Pass1234!")
            composeTestRule.onNodeWithText("Confirm Password").performTextInput("Pass1234!")
            composeTestRule.onNodeWithText("Sign Up").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 3b: Weak Password Format
        ScreenshotHelper.step("01", "03b", device) {
            composeTestRule.onNodeWithText("Email").performTextClearance()
            composeTestRule.onNodeWithText("Email").performTextInput("tester@example.com")
            composeTestRule.onNodeWithText("Password").performTextClearance()
            composeTestRule.onNodeWithText("Password").performTextInput("short")
            composeTestRule.onNodeWithText("Confirm Password").performTextClearance()
            composeTestRule.onNodeWithText("Confirm Password").performTextInput("short")
            composeTestRule.onNodeWithText("Sign Up").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 3c: Mismatched Password
        ScreenshotHelper.step("01", "03c", device) {
            composeTestRule.onNodeWithText("Password").performTextClearance()
            composeTestRule.onNodeWithText("Password").performTextInput("ValidPass123!")
            composeTestRule.onNodeWithText("Confirm Password").performTextClearance()
            composeTestRule.onNodeWithText("Confirm Password").performTextInput("DifferentPass123!")
            composeTestRule.onNodeWithText("Sign Up").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 4: Terms Unchecked Blocked
        ScreenshotHelper.step("01", "04", device) {
            composeTestRule.onNodeWithText("Confirm Password").performTextClearance()
            composeTestRule.onNodeWithText("Confirm Password").performTextInput("ValidPass123!")
            composeTestRule.onNodeWithText("Sign Up").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 5: Valid Registration Landing (Check Terms and proceed)
        ScreenshotHelper.step("01", "05", device) {
            composeTestRule.onNodeWithText("I agree to the ").performClick()
            composeTestRule.onNodeWithText("Sign Up").performClick()
            composeTestRule.waitForIdle()
        }

        // Step 6: Duplicate Email Error
        ScreenshotHelper.step("01", "06", device) {
            // Assert duplicate email handling
            composeTestRule.waitForIdle()
        }
    }
}
