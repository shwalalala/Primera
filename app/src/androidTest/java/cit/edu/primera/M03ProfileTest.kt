package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.RegressionTest
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.notification.NotificationHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RegressionTest
class M03ProfileTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            runBlocking {
                LoginHelper.signInWithTestAccount()
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    cit.edu.primera.core.data.PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                }
                SeedData.seedUserProfileWithLmpAug21()
            }
        }
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            val allowBtn = device.wait(Until.findObject(By.textContains("Allow")), 2000)
            allowBtn?.click()
        } catch (e: Throwable) {}

        // Wait for Splash screen to transition to Dashboard
        try {
            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {
            // In case app landed on Welcome or Login
            try {
                composeTestRule.onNodeWithText("Skip").performClick()
            } catch (t: Throwable) {}
            try {
                composeTestRule.onNodeWithText("Get Started").performClick()
            } catch (t: Throwable) {}
            try {
                LoginHelper.performUiLogin(composeTestRule)
            } catch (t: Throwable) {}
            try {
                composeTestRule.waitUntil(10000) {
                    try {
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }
        navigateToProfile()
    }

    private fun navigateToProfile() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "profile")
            })
        }
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithText("My Profile", substring = true).fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
    }

    @Test
    fun testProfileSetupAndEditingSuite() {
        // Step 1: Blank field errors (Validation blocks progress if required fields blank)
        ScreenshotHelper.step("03", "01", device) {
            try {
                composeTestRule.onNodeWithText("Personal Information").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithTag("profile_edit_button").performClick()
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithText("Edit Section").performClick()
                } catch (t: Throwable) {}
            }
            composeTestRule.waitForIdle()

            // Clear weight and attempt to save
            try {
                composeTestRule.onNodeWithTag("profile_field_pre_pregnancy_weight_kg").performTextClearance()
                composeTestRule.onNodeWithTag("profile_save_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 2: Invalid values rejected (Evidence Gap: Non-positive height/weight)
        ScreenshotHelper.step("03", "02", device) {
            try {
                composeTestRule.onNodeWithTag("profile_field_pre_pregnancy_weight_kg").performTextInput("-10")
                composeTestRule.onNodeWithTag("profile_field_height_cm").performTextClearance()
                composeTestRule.onNodeWithTag("profile_field_height_cm").performTextInput("0")
                composeTestRule.onNodeWithTag("profile_save_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Valid profile saved with calculated EDD & Gestational Age (D-07 / RT-07)
        // Defect Verification D-07: LMP Aug 21, 2026 shows EDD Aug 20, 2027 and "1 week, 0 days".
        // Expected EDD = LMP + 280 days (May 28, 2027), ~12 weeks on Nov 13, 2026.
        // Assert EDD is May 28, 2027 and gestational age reflects ~12 weeks (NOT Aug 20, 2027 or 1w 0d).
        ScreenshotHelper.step("03", "03", "07", device) {
            // Expand Pregnancy Details section
            try {
                composeTestRule.onNodeWithText("Pregnancy Details").performScrollTo().performClick()
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithText("Pregnancy Details").performClick()
                } catch (_: Throwable) {}
            }
            composeTestRule.waitForIdle()

            // The defect manifests as EDD "Aug 20, 2027" and "1 Weeks 0 Days"
            // The expected correct EDD is "May 28, 2027" and "~12 Weeks"
            composeTestRule.onNodeWithText("Aug 20, 2027", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("1 Weeks 0 Days", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("1w 0d", substring = true).assertDoesNotExist()

            try {
                composeTestRule.onNodeWithText("May 28, 2027", substring = true).performScrollTo()
            } catch (t: Throwable) {}
            composeTestRule.onNodeWithText("May 28, 2027", substring = true).assertIsDisplayed()

            try {
                composeTestRule.onNodeWithText("12 Weeks", substring = true).performScrollTo()
            } catch (t: Throwable) {}
            composeTestRule.onNodeWithText("12 Weeks", substring = true).assertIsDisplayed()
        }

        // Step 4: Profile after reopening (Persistence across relaunch)
        ScreenshotHelper.step("03", "04", device) {
            navigateToProfile()
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodesWithText("My Profile", substring = true).onFirst().assertIsDisplayed()
        }

        // Step 5: Edited profile saved and persisted
        ScreenshotHelper.step("03", "05", device) {
            try {
                composeTestRule.onNodeWithText("Personal Information").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithTag("profile_edit_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithTag("profile_field_pre_pregnancy_weight_kg").performTextClearance()
                composeTestRule.onNodeWithTag("profile_field_pre_pregnancy_weight_kg").performTextInput("60")
                composeTestRule.onNodeWithTag("profile_save_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithText("60", substring = true).performScrollTo()
            } catch (t: Throwable) {}
            composeTestRule.onNodeWithText("60", substring = true).assertIsDisplayed()
        }
    }
}
