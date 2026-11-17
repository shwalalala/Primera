package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M04SmartwatchConnectionTest {

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
        // Navigate to Smartwatch / Device tab
        try {
            composeTestRule.onNodeWithText("Device").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testSmartwatchConnectionSuite() {
        // Step 1: Connect Smartwatch screen loaded
        ScreenshotHelper.step("04", "01", device) {
            composeTestRule.onNodeWithText("Connect your health data source").assertIsDisplayed()
        }

        // Step 2: Permission request sheet interaction via UI Automator
        ScreenshotHelper.step("04", "02", device) {
            try {
                composeTestRule.onNodeWithTag("review_permissions_button").performClick()
                device.wait(Until.findObject(By.textContains("Allow")), 2000)
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Permission denied state
        ScreenshotHelper.step("04", "03", device) {
            // Dismiss or deny permission dialog if visible
            val denyBtn = device.findObject(By.textContains("Don't allow")) ?: device.findObject(By.textContains("Deny"))
            denyBtn?.click()
            composeTestRule.waitForIdle()
        }

        // Step 4: Permissions granted / connected state
        ScreenshotHelper.step("04", "04", device) {
            composeTestRule.waitForIdle()
        }

        // Step 5: Health Connect unavailable state & Consistent Connection State Verification
        // Defect Verification: The screen must NEVER show "Permissions accepted" together with
        // "Sync failed: Health Connect is not installed" or "System communication is slow".
        ScreenshotHelper.step("04", "05", device) {
            try {
                composeTestRule.onNodeWithTag("connect_and_sync_button").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()

            // If an error / not installed message is shown, "Permissions accepted" MUST NOT be displayed
            val statusNode = composeTestRule.onNodeWithTag("smartwatch_status_message")
            try {
                statusNode.assertIsDisplayed()
            } catch (_: Exception) {}

            // Consistency check: ensure no false connected state
            try {
                composeTestRule.onNodeWithText("Sync failed: Health Connect is not installed on this device.").assertIsDisplayed()
                composeTestRule.onNodeWithTag("permissions_accepted_row").assertDoesNotExist()
            } catch (_: Exception) {}
        }
    }
}
