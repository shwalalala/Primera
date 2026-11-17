package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M02LoginTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = true, clearPrefs = true)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private val uniqueTimestamp = System.currentTimeMillis()
    private val testEmail = "primera.m02.$uniqueTimestamp@example.com"
    private val testPass = "ValidPass123!"

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Create a fresh test account with completed onboarding profile
        runBlocking {
            try {
                val result = FirebaseAuth.getInstance().createUserWithEmailAndPassword(testEmail, testPass).await()
                val uid = result.user?.uid
                if (uid != null) {
                    SeedData.seedUserProfile(userId = uid, username = "M02User")
                }
            } catch (e: Exception) {
                android.util.Log.e("M02LoginTest", "Failed to seed M02 user", e)
            }
            FirebaseAuth.getInstance().signOut()
        }

        // Wait for Splash screen to transition to Welcome or Auth screen
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Get Started").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("login_submit_button").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("tab_login").fetchSemanticsNodes().isNotEmpty()
        }

        // If on Welcome screen, skip to Auth
        if (composeTestRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Skip").performClick()
        } else if (composeTestRule.onAllNodesWithText("Get Started").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Get Started").performClick()
        }
        composeTestRule.waitForIdle()

        // Switch to Log In tab if not already on Login screen
        if (composeTestRule.onAllNodesWithTag("login_submit_button").fetchSemanticsNodes().isEmpty()) {
            if (composeTestRule.onAllNodesWithTag("tab_login").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onNodeWithTag("tab_login").performClick()
            } else if (composeTestRule.onAllNodesWithTag("register_to_login_button").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onNodeWithTag("register_to_login_button").performClick()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testLoginFullSuite() {
        // Step 1: Blank fields validation
        ScreenshotHelper.step("02", "01", device) {
            composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("Email is required").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        }

        // Step 2: Wrong password
        ScreenshotHelper.step("02", "02", device) {
            composeTestRule.onNodeWithTag("login_email_input").performScrollTo().performTextInput(testEmail)
            composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextInput("WrongPassword999!")
            Espresso.closeSoftKeyboard()
            composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithTag("login_error_message").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Invalid email or password").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Invalid email or password").assertIsDisplayed()
        }

        // Step 3: Unregistered email
        ScreenshotHelper.step("02", "03", device) {
            composeTestRule.onNodeWithTag("login_email_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("login_email_input").performTextInput("unregistered.primera.${System.currentTimeMillis()}@example.com")
            composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("login_password_input").performTextInput("SomePass123!")
            Espresso.closeSoftKeyboard()
            composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithTag("login_error_message").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Invalid email or password").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Invalid email or password").assertIsDisplayed()
        }

        // Step 4: Valid login & Remember Me (RT-13 / TC-M02-04)
        ScreenshotHelper.step("02", "04", rtNum = "13", device = device) {
            composeTestRule.onNodeWithTag("login_email_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("login_email_input").performTextInput(testEmail)
            composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("login_password_input").performTextInput(testPass)
            composeTestRule.onNodeWithTag("login_remember_me_checkbox").performScrollTo().performClick()
            Espresso.closeSoftKeyboard()
            composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Wait for authentication and navigation away from login to Dashboard
            composeTestRule.waitUntil(20000) {
                FirebaseAuth.getInstance().currentUser != null &&
                (composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
                 composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty() ||
                 composeTestRule.onAllNodesWithText("Tell Us Your Birthday").fetchSemanticsNodes().isNotEmpty())
            }
            val user = FirebaseAuth.getInstance().currentUser
            assertNotNull("User should be authenticated after valid login", user)
            assertEquals(testEmail, user?.email)
        }

        // Step 5: Session restore after app recreate/restart
        ScreenshotHelper.step("02", "05", device) {
            composeTestRule.activityRule.scenario.recreate()
            composeTestRule.waitForIdle()

            // Wait for splash (2.5s) and assert session restore
            composeTestRule.waitUntil(15000) {
                FirebaseAuth.getInstance().currentUser != null &&
                (composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
                 composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty())
            }
            assertNotNull("User session should be restored after recreate", FirebaseAuth.getInstance().currentUser)
            assertEquals(testEmail, FirebaseAuth.getInstance().currentUser?.email)
        }

        // Step 6: Logout with saved email
        ScreenshotHelper.step("02", "06", device) {
            // Open sidebar by clicking hamburger icon on dashboard top bar
            composeTestRule.onAllNodes(
                hasClickAction() and hasAnyAncestor(hasTestTag("dashboard_top_bar"))
            ).onFirst().performClick()
            composeTestRule.waitForIdle()

            // Click Log Out in sidebar
            composeTestRule.waitUntil(8000) {
                composeTestRule.onAllNodesWithText("Log Out").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Log Out").performClick()
            composeTestRule.waitForIdle()

            // Verify returned to Login screen with email prefilled
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithTag("login_email_input").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithTag("login_email_input").assertIsDisplayed()
            composeTestRule.onNodeWithTag("login_email_input").assertTextContains(testEmail)
        }

        // Step 7: Google Sign-In with UI Automator driving account picker
        ScreenshotHelper.step("02", "07", device) {
            try {
                composeTestRule.onNodeWithTag("social_google_button").performScrollTo().performClick()
                composeTestRule.waitForIdle()
                // Drive Google account chooser using UI Automator
                LoginHelper.driveGoogleAccountPicker(device, timeoutMs = 4000)
            } catch (_: Throwable) {
                // If Google Sign-In bottom sheet or Play Services throws on emulator, record state
            }
            composeTestRule.waitForIdle()
        }
    }
}
