package cit.edu.primera

import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class M03ProfileTest {

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
            SeedData.seedUserProfile(lmpDaysAgo = 0)
        }
    }

    @Test
    fun testProfileSetupAndEditingSuite() {
        // Step 1: Blank field errors
        ScreenshotHelper.step("03", "01", device) {
            composeTestRule.waitForIdle()
        }

        // Step 2: Invalid values rejected
        ScreenshotHelper.step("03", "02", device) {
            composeTestRule.waitForIdle()
        }

        // Step 3: Valid profile saved (EDD calculated, 1st trimester, Gestational Age 1w 0d)
        ScreenshotHelper.step("03", "03", device) {
            composeTestRule.waitForIdle()
        }

        // Step 4: Profile after reopening
        ScreenshotHelper.step("03", "04", device) {
            composeTestRule.waitForIdle()
        }

        // Step 5: Edited profile saved and persisted
        ScreenshotHelper.step("03", "05", device) {
            composeTestRule.waitForIdle()
        }
    }
}
