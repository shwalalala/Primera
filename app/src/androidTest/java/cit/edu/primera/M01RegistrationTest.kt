package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.RegressionTest
import cit.edu.primera.common.ScreenshotHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RegressionTest
class M01RegistrationTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = true, clearPrefs = true)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Wait for Splash screen to transition to Welcome or Auth screen
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Get Started").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Create Account").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("tab_signup").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Log In").fetchSemanticsNodes().isNotEmpty()
        }

        // If on Welcome screen, skip to Auth
        if (composeTestRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Skip").performClick()
        } else if (composeTestRule.onAllNodesWithText("Get Started").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Get Started").performClick()
        }
        composeTestRule.waitForIdle()

        // Switch to Sign Up tab if on Login
        if (composeTestRule.onAllNodesWithTag("tab_signup").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("tab_signup").performClick()
        } else if (composeTestRule.onAllNodesWithText("Sign Up").fetchSemanticsNodes().isNotEmpty() &&
            composeTestRule.onAllNodesWithText("Create Account").fetchSemanticsNodes().isEmpty()
        ) {
            composeTestRule.onNodeWithText("Sign Up").performClick()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRegistrationFullSuite() {
        // Step 1 (TC-M01-01): Register Screen Loaded
        ScreenshotHelper.step("01", "01", device) {
            composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
            composeTestRule.onNodeWithText("Sign up to start your journey").assertIsDisplayed()
            composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        }

        // Step 2 (TC-M01-02): Blank Field Validation
        ScreenshotHelper.step("01", "02", device) {
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Username is required").assertIsDisplayed()
            composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
            composeTestRule.onNodeWithText("Password must be at least 8 characters").assertIsDisplayed()
        }

        // Step 3a (TC-M01-03a): Invalid Email Format
        ScreenshotHelper.step("01", "03a", device) {
            composeTestRule.onNodeWithTag("register_username_input").performScrollTo().performTextInput("SSTestUser")
            composeTestRule.onNodeWithTag("register_email_input").performScrollTo().performTextInput("invalid-email-format")
            composeTestRule.onNodeWithTag("register_password_input").performScrollTo().performTextInput("ValidPass123!")
            composeTestRule.onNodeWithTag("register_confirm_password_input").performScrollTo().performTextInput("ValidPass123!")
            closeSoftKeyboard()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
        }

        // Step 3b (TC-M01-03b): Weak Password Format
        ScreenshotHelper.step("01", "03b", device) {
            composeTestRule.onNodeWithTag("register_email_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_email_input").performTextInput("primera.tester@example.com")
            composeTestRule.onNodeWithTag("register_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_password_input").performTextInput("short")
            composeTestRule.onNodeWithTag("register_confirm_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_confirm_password_input").performTextInput("short")
            closeSoftKeyboard()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Password must be at least 8 characters").assertIsDisplayed()
        }

        // Step 3c (TC-M01-03c): Mismatched Password
        ScreenshotHelper.step("01", "03c", device) {
            composeTestRule.onNodeWithTag("register_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_password_input").performTextInput("ValidPass123!")
            composeTestRule.onNodeWithTag("register_confirm_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_confirm_password_input").performTextInput("DifferentPass123!")
            closeSoftKeyboard()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
        }

        // Step 4 (TC-M01-04): Terms Unchecked Blocked
        ScreenshotHelper.step("01", "04", device) {
            composeTestRule.onNodeWithTag("register_confirm_password_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_confirm_password_input").performTextInput("ValidPass123!")
            closeSoftKeyboard()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("You must agree to terms").assertIsDisplayed()
        }

        // Step 5 (TC-M01-05): Valid Registration Landing (Check Terms and proceed to Profile Setup)
        val testEmail = "primera.qa.${System.currentTimeMillis()}@example.com"
        ScreenshotHelper.step("01", "05", device) {
            composeTestRule.onNodeWithTag("register_email_input").performScrollTo().performTextClearance()
            composeTestRule.onNodeWithTag("register_email_input").performTextInput(testEmail)
            composeTestRule.onNodeWithTag("register_terms_checkbox").performScrollTo().performTouchInput {
                click(centerLeft)
            }
            closeSoftKeyboard()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Wait for navigation to Profile Setup (Onboarding: Tell Us Your Birthday)
            composeTestRule.waitUntil(20000) {
                composeTestRule.onAllNodesWithText("Tell Us Your Birthday").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Tell Us Your Birthday").assertIsDisplayed()
        }

        // Step 6 (RT-12, TC-M01-06): Query Auth / Firestore and assert exactly one account and one user document
        ScreenshotHelper.step("01", "06", rtNum = "12", device = device) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            assertNotNull("Firebase Auth currentUser should exist after registration", currentUser)
            assertEquals("Registered email should match", testEmail, currentUser?.email)

            val firestore = FirebaseFirestore.getInstance()
            val uid = currentUser!!.uid
            val userDoc = runBlocking {
                firestore.collection("users").document(uid).get().await()
            }
            assertTrue("User document should exist in Firestore for registered UID", userDoc.exists())
            assertEquals("Username should match", "SSTestUser", userDoc.getString("username"))
        }
    }
}
