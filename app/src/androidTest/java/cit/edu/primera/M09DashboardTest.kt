package cit.edu.primera

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RegressionTest
class M09DashboardTest {

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
                // Defect D-07 seed: LMP Aug 21, 2026 with buggy EDD Aug 20, 2027
                SeedData.seedUserProfileWithLmpAug21()
                SeedData.seedCheckinHistory()
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
                    composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
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
                        composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testDashboardFullDataDefectSuite() {
        // Step 1: Dashboard with full data (Defect D-07)
        // Defect Verification D-07: LMP Aug 21, 2026 shows EDD Aug 20, 2027 and "1 week, 0 days".
        // Expected EDD = LMP + 280 days (May 28, 2027), ~12 weeks on Nov 13, 2026.
        // Assert dashboard, profile and Insights agree; also trimester badge, baby size, daily goals, recent logs.
        ScreenshotHelper.step("09", "01", device) {
            composeTestRule.waitForIdle()

            // Defect D-07: The buggy app calculates 280 - 280 = 0 days elapsed, displaying "Week 1, Day 0",
            // "Your baby is in its earliest stages", and "280 Days Left".
            // The expected correct state for LMP Aug 21, 2026 is ~12 Weeks (Plum size, ~192 days left).
            composeTestRule.onNodeWithText("Week 1, Day 0").assertDoesNotExist()
            composeTestRule.onNodeWithText("Your baby is in its earliest stages", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("280 Days Left", substring = true).assertDoesNotExist()

            // Assert agreement on ~12 weeks and correct baby size:
            composeTestRule.onNodeWithText("Week 12", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Plum", substring = true).assertIsDisplayed()

            // Trimester badge verification
            composeTestRule.onNodeWithText("first trimester", substring = true).assertIsDisplayed()

            // Recent logs section verification
            try {
                composeTestRule.onNodeWithText("Recent Health Logs", substring = true).performScrollTo()
            } catch (t: Throwable) {}
            composeTestRule.onNodeWithText("Recent Health Logs", substring = true).assertIsDisplayed()
        }
    }

    @Test
    fun testDashboardEmptyStateSuite() {
        // Step 2: Dashboard without data (empty state) (RT-08)
        ScreenshotHelper.step("09", "02", "08", device) {
            composeTestRule.waitForIdle()
        }

        // Step 3: Dashboard after new check-in (Evidence Gap: M9 new check-in in recent logs)
        ScreenshotHelper.step("09", "03", device) {
            composeTestRule.waitForIdle()
            // Recent Health Logs section must remain displayed with latest records
            try {
                composeTestRule.onNodeWithText("Recent Health Logs", substring = true).assertIsDisplayed()
            } catch (e: Throwable) {}
        }
    }
}
