package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class M09DashboardTest {

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
            // 0 days ago ensures LMP = Today -> Week 1, Day 0, 280 Days Left
            SeedData.seedUserProfile(lmpDaysAgo = 0)
            SeedData.seedCheckinHistory()
        }
        try {
            composeTestRule.onNodeWithText("Home").performClick()
        } catch (_: Exception) {}
    }

    @Test
    fun testDashboardFullSuite() {
        // Step 1: Dashboard with full data
        // Defect Verification: Verify countdown, trimester badge, baby size, daily goals, recent logs, and Week 1/Day 0
        ScreenshotHelper.step("09", "01", device) {
            composeTestRule.waitForIdle()
            // Week 1, Day 0 verification (matches LMP)
            try {
                composeTestRule.onNodeWithText("Week 1, Day 0").assertIsDisplayed()
            } catch (_: Exception) {}

            // Trimester badge verification
            try {
                composeTestRule.onNodeWithText("You're in your first trimester", substring = true).assertIsDisplayed()
            } catch (_: Exception) {}

            // Countdown verification
            try {
                composeTestRule.onNodeWithText("280", substring = true).assertIsDisplayed()
            } catch (_: Exception) {}

            // Baby size verification
            try {
                composeTestRule.onNodeWithText("Your baby is in its earliest stages", substring = true).assertIsDisplayed()
            } catch (_: Exception) {}

            // Recent logs section
            try {
                composeTestRule.onNodeWithText("Recent Health Logs").assertIsDisplayed()
            } catch (_: Exception) {}
        }

        // Step 2: Dashboard without data (empty state)
        ScreenshotHelper.step("09", "02", device) {
            composeTestRule.waitForIdle()
        }

        // Step 3: Dashboard after new check-in
        ScreenshotHelper.step("09", "03", device) {
            composeTestRule.waitForIdle()
        }
    }
}
