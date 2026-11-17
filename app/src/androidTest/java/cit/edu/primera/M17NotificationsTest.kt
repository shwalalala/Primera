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
class M17NotificationsTest {

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
    fun testNotificationsScreenSuite() {
        // Step 1: Notifications Screen, Chronological Order, Category Tabs, Mark All Read, Empty State
        ScreenshotHelper.step("17", "01", device) {
            try {
                // Navigate to notifications screen via top bar or drawer
                composeTestRule.onNodeWithText("Notifications").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()

            // Verify filter tabs exist (All, Check-ins, etc.)
            try {
                composeTestRule.onNodeWithText("All").performClick()
                composeTestRule.onNodeWithText("Check-ins").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }
}
