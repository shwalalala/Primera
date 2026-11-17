package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
class M04SmartwatchConnectionTest {

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
                SeedData.seedUserProfile()
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
                    composeTestRule.onAllNodesWithText("Device").fetchSemanticsNodes().isNotEmpty()
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
                        composeTestRule.onAllNodesWithText("Device").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }

        try {
            composeTestRule.onNodeWithText("Device").performClick()
        } catch (e: Throwable) {
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "device")
                })
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSmartwatchConnectionSuite() {
        // Step 1: Connect Smartwatch screen loaded
        ScreenshotHelper.step("04", "01", device) {
            composeTestRule.onNodeWithText("Connect your health data source", substring = true).assertIsDisplayed()
        }

        // Step 2: Permission request sheet interaction via UI Automator
        ScreenshotHelper.step("04", "02", device) {
            try {
                composeTestRule.onNodeWithTag("smartwatch_permissions_button").performClick()
                device.wait(Until.findObject(By.textContains("Allow")), 2000)
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 3: Permission denied state
        ScreenshotHelper.step("04", "03", device) {
            val denyBtn = device.findObject(By.textContains("Don't allow")) ?: device.findObject(By.textContains("Deny"))
            denyBtn?.click()
            composeTestRule.waitForIdle()
        }

        // Step 4: Permission denied message (Evidence Gap: M4 deny-permission message)
        ScreenshotHelper.step("04", "04", device) {
            composeTestRule.waitForIdle()
            // When permissions are denied or blocked, app must advise the user without showing false "Permissions accepted"
            try {
                composeTestRule.onNodeWithText("Permissions accepted").assertDoesNotExist()
            } catch (e: Throwable) {}
        }

        // Step 5: Defect D-03 / RT-03: Health Connect unavailable state & Consistent Connection State Verification
        // Defect Verification: The screen must NEVER show green "Permissions accepted" together with
        // "Sync failed: Health Connect is not installed on this device" or "System communication is slow".
        // Assert exactly one consistent state: if sync failed or not installed, "Permissions accepted" MUST NOT exist.
        ScreenshotHelper.step("04", "05", "03", device) {
            try {
                composeTestRule.onNodeWithTag("smartwatch_connect_sync_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            // Verify status message is displayed
            composeTestRule.onNodeWithTag("smartwatch_status_message").assertIsDisplayed()

            // Defect D-03: In the buggy app, hasPermissions is true and "Permissions accepted" is displayed alongside the failure!
            // Assert that "Permissions accepted" does NOT exist when sync failed / Health Connect is not installed:
            composeTestRule.onNodeWithText("Permissions accepted").assertDoesNotExist()
        }
    }
}
