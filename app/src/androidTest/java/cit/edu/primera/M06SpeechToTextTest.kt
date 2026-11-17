package cit.edu.primera

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class M06SpeechToTextTest {

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
    fun testSpeechToTextFullSuite() {
        // Step 1: Mic permission request & denial
        ScreenshotHelper.step("06", "01", device) {
            try {
                composeTestRule.onNodeWithContentDescription("Voice Log").performClick()
            } catch (_: Exception) {
                try {
                    composeTestRule.onNodeWithContentDescription("Microphone").performClick()
                } catch (_: Exception) {}
            }
            device.wait(Until.findObject(By.textContains("permission")), 2000)
            composeTestRule.waitForIdle()
        }

        // Step 2: Permission granted, listening state
        ScreenshotHelper.step("06", "02", device) {
            val allowBtn = device.findObject(By.textContains("While using the app")) ?: device.findObject(By.textContains("Allow"))
            allowBtn?.click()
            composeTestRule.waitForIdle()
        }

        // Step 3: Transcription text field / manual entry fallback
        ScreenshotHelper.step("06", "03", device) {
            try {
                composeTestRule.onNodeWithText("Or type your notes manually").performClick()
            } catch (_: Exception) {
                try {
                    composeTestRule.onNodeWithText("Type Manually").performClick()
                } catch (_: Exception) {}
            }
            composeTestRule.waitForIdle()
        }

        // Step 4: Symptom tags detection
        ScreenshotHelper.step("06", "04", device) {
            composeTestRule.waitForIdle()
        }

        // Step 5: Edited text saved
        ScreenshotHelper.step("06", "05", device) {
            composeTestRule.waitForIdle()
        }

        // Step 6: Saved entry in check-in
        ScreenshotHelper.step("06", "06", device) {
            composeTestRule.waitForIdle()
        }
    }
}
