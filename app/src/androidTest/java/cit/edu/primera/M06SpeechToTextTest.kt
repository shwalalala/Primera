package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M06SpeechToTextTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            runBlocking {
                LoginHelper.signInWithTestAccount()
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    cit.edu.primera.core.data.PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                    SeedData.seedUserProfile(uid)
                }
            }
        }
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Wait for Splash screen to transition to Dashboard
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSpeechToTextFullSuite() {
        // Step 1: Microphone permission request & denial
        ScreenshotHelper.step("06", "01", device) {
            // Revoke RECORD_AUDIO permission to verify permission prompt
            device.executeShellCommand("pm revoke cit.edu.primera android.permission.RECORD_AUDIO")

            // Navigate to Voice Logging via center FAB on Dashboard
            composeTestRule.onAllNodesWithContentDescription("Microphone").onFirst().performClick()
            composeTestRule.waitForIdle()

            composeTestRule.waitUntil(8000) {
                composeTestRule.onAllNodesWithText("Voice Logging").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Voice Logging").assertIsDisplayed()

            // Click Microphone button inside Voice Logging to request permission
            composeTestRule.onAllNodesWithContentDescription("Microphone").onFirst().performClick()
            composeTestRule.waitForIdle()

            // UI Automator checks for permission dialog and denies it
            val denyBtn = device.wait(Until.findObject(By.textContains("Don't allow")), 2000)
                ?: device.wait(Until.findObject(By.textContains("Deny")), 1000)
                ?: device.findObject(By.text("Don't allow"))
                ?: device.findObject(By.text("Deny"))
            denyBtn?.click()
            composeTestRule.waitForIdle()
        }

        // Step 2: Grant permission, listening state and silence/notice handling
        ScreenshotHelper.step("06", "02", device) {
            // Grant RECORD_AUDIO permission
            device.executeShellCommand("pm grant cit.edu.primera android.permission.RECORD_AUDIO")
            composeTestRule.waitForIdle()

            // Click Microphone button to start listening
            composeTestRule.onAllNodesWithContentDescription("Microphone").onFirst().performClick()
            composeTestRule.waitForIdle()

            // Verify listening state or speech notice dialog
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("Listening in", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Speech Recognition Notice").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitForIdle()

            // Stop listening (silence case on emulator triggers speech recognition notice dialog)
            if (composeTestRule.onAllNodesWithText("Speech Recognition Notice").fetchSemanticsNodes().isEmpty()) {
                composeTestRule.onAllNodesWithContentDescription("Microphone").onFirst().performClick()
                composeTestRule.waitForIdle()
            }
        }

        // Step 3 (RT-14 / TC-M06-03): Manual entry fallback & AI punctuation
        ScreenshotHelper.step("06", "03", rtNum = "14", device = device) {
            // Wait for Speech Recognition Notice dialog or idle
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Speech Recognition Notice").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Type Manually").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Or type your notes manually").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Voice Log (AI-Corrected)").fetchSemanticsNodes().isNotEmpty()
            }

            if (composeTestRule.onAllNodesWithText("Type Manually").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onNodeWithText("Type Manually").performClick()
            } else if (composeTestRule.onAllNodesWithText("Or type your notes manually").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onNodeWithText("Or type your notes manually").performClick()
            }
            composeTestRule.waitForIdle()

            // Input symptoms text into Voice Log text field
            val noteText = "I have mild nausea and headache since this morning"
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Voice Log (AI-Corrected)").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Voice Log (AI-Corrected)").performTextInput(noteText)
            Espresso.closeSoftKeyboard()
            composeTestRule.waitForIdle()

            // Click AI Polish
            composeTestRule.onNodeWithText("AI Polish").performClick()
            composeTestRule.waitForIdle()

            // Wait for formatting completion and verify Save button is ready
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Save to Firestore").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Save to Firestore").assertIsDisplayed()
        }

        // Step 4 (TC-M06-04): Save to Firestore
        ScreenshotHelper.step("06", "04", device) {
            composeTestRule.onNodeWithText("Save to Firestore").performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Wait for upload completion and assert Saved! button text
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Saved!").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Saved!").assertIsDisplayed()
        }

        // Step 5 (TC-M06-05): Use in Check-In
        ScreenshotHelper.step("06", "05", device) {
            composeTestRule.onNodeWithText("Use in Check-In").performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Assert navigated to Daily Check-in screen
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Daily Check-in").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Daily Log").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Symptoms").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitForIdle()
        }
    }
}
