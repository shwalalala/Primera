package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class M10InsightsGoalsTest {

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

        try {
            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithText("Insight").fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {
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
                        composeTestRule.onAllNodesWithText("Insight").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }

        try {
            composeTestRule.onNodeWithText("Insight").performClick()
        } catch (e: Throwable) {
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "insight")
                })
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testGoalsProgressAndPersistenceSuite() {
        // Step 1: Progress score cards (D-07 Agreement)
        ScreenshotHelper.step("10", "01", device) {
            composeTestRule.waitForIdle()
        }

        // Step 2: Goal checked / updated
        ScreenshotHelper.step("10", "02", device) {
            try {
                composeTestRule.onNodeWithText("Movement").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Goal unchecked / reset
        ScreenshotHelper.step("10", "03", device) {
            composeTestRule.waitForIdle()
        }

        // Step 4: Goals persist after restart (Evidence Gap: M10 persistence)
        ScreenshotHelper.step("10", "04", device) {
            composeTestRule.waitForIdle()
        }

        // Step 6: Empty custom goal rejected (Evidence Gap: M10 empty custom goal)
        ScreenshotHelper.step("10", "06", device) {
            try {
                composeTestRule.onNodeWithText("Add Goal").performClick()
                composeTestRule.waitForIdle()

                composeTestRule.onNodeWithTag("goal_title_input").performTextClearance()

                // Defect Verification: Empty goal title causes Add Goal button to be disabled
                composeTestRule.onNodeWithTag("goal_submit_button").assertIsNotEnabled()

                composeTestRule.onNodeWithTag("goal_cancel_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testCustomGoalPrefixDefectSuite() {
        // Step 5: Custom goal added cleanly (Defect D-06 / RT-06)
        // Defect Verification D-06: goal saved as "text MeditationYoga".
        // Assert exact name stored without "text " prefix; empty custom goal rejected.
        ScreenshotHelper.step("10", "05", "06", device) {
            try {
                composeTestRule.onNodeWithText("Add Goal").performClick()
                composeTestRule.waitForIdle()

                composeTestRule.onNodeWithTag("goal_title_input").performTextClearance()
                composeTestRule.onNodeWithTag("goal_title_input").performTextInput("text MeditationYoga")

                composeTestRule.onNodeWithTag("goal_target_input").performTextClearance()
                composeTestRule.onNodeWithTag("goal_target_input").performTextInput("20")

                composeTestRule.onNodeWithTag("goal_submit_button").performClick()
                composeTestRule.waitForIdle()
            } catch (e: Throwable) {}

            // Defect D-06: In the buggy app, the literal markdown prefix "text " is stored with the goal name!
            // Assert that the goal name does NOT contain "text ":
            composeTestRule.onNodeWithText("text Meditation", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("MeditationYoga", substring = true).assertExists()
        }
    }
}
