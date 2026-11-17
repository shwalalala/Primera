package cit.edu.primera

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.RegressionTest
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test verifying test setup, common helpers, and screenshot capture.
 */
@RunWith(AndroidJUnit4::class)
@RegressionTest
class SmokeSetupTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testSetupHelpersAndScreenshot() {
        assertNotNull("SeedData test email should not be null", SeedData.TEST_USER_EMAIL)
        assertNotNull("ClearStateRule should be initialized", clearStateRule)
        assertNotNull("LoginHelper should be available", LoginHelper)

        ScreenshotHelper.step("00", "01", device) {
            composeTestRule.waitForIdle()
            assertNotNull("Activity should be launched", composeTestRule.activity)
            composeTestRule.onRoot().assertExists()
        }

        ScreenshotHelper.regressionStep(1, device) {
            composeTestRule.waitForIdle()
            assertNotNull("Activity should be launched", composeTestRule.activity)
        }
    }
}
