package cit.edu.primera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * M22 Security Verification Suite:
 *
 * 1. Protected Screen Redirection:
 *    Unauthenticated navigation directly redirects to Login / Welcome, shielding user data.
 *
 * 2. QR / Export Permission Audit:
 *    Health summary QR & PDF export dialog loaded, testing export options (Save as PDF, Print, Share).
 *
 * 3. Component Security Audit:
 *    Verifies non-exported components (e.g. ReminderReceiver exported="false").
 *
 * -------------------------------------------------------------------------------------------------
 * PROCEDURES OUTSIDE INSTRUMENTED TESTING (Manual / CI Commands):
 *
 * A. HTTPS Network Interception & Proxying (mitmproxy):
 *    Command to configure device proxy:
 *      adb shell settings put global http_proxy <HOST_IP>:8080
 *    Start mitmproxy on host:
 *      mitmproxy -p 8080
 *    Verification:
 *      Observe network traffic across Firebase Auth, Firestore, and Gemini APIs.
 *      Verify TLS certificate pinning and strict HTTPS transport encryption prevent MITM inspection.
 *    Restore clean settings:
 *      adb shell settings put global http_proxy :0
 *
 * B. Encryption at Rest & App Sandbox Isolation:
 *    Inspect private sandbox files and databases:
 *      adb shell run-as cit.edu.primera ls -la /data/data/cit.edu.primera/
 *      adb shell run-as cit.edu.primera ls -la /data/data/cit.edu.primera/shared_prefs/
 *      adb shell run-as cit.edu.primera cat /data/data/cit.edu.primera/shared_prefs/primera_app_preferences.xml
 *      adb shell run-as cit.edu.primera ls -la /data/data/cit.edu.primera/databases/
 *    Verification:
 *      Verify that sensitive auth tokens, passwords, and biometrics are never stored in plaintext,
 *      and application databases and preferences are secured inside private sandbox storage.
 *
 * C. Cloud Firestore Security Rules Unit Testing:
 *    Execute automated rules unit test suite with Firebase Emulator Suite:
 *      firebase emulators:exec --only firestore "npm test"
 *    Verification:
 *      Rules test uses @firebase/rules-unit-testing to assert that:
 *      - Unauthenticated reads and writes are denied.
 *      - Authenticated users can only read/write documents where request.auth.uid == userId.
 *      - Cross-tenant data access is strictly blocked.
 * -------------------------------------------------------------------------------------------------
 */
@RunWith(AndroidJUnit4::class)
class M22SecurityTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun test01_protectedScreensRedirectToLoginWhenLoggedOut() {
        // Step 1: Sign out and assert protected screen deep link redirects to Login/Welcome
        ScreenshotHelper.step("22", "01", device) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "checkin")
            }
            context.startActivity(intent)
            composeTestRule.waitForIdle()

            // Verify landing on Welcome or Login, not protected Check-in
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithText("Log In").fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("Welcome to Primera").fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("Sign In").fetchSemanticsNodes().isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }
            // Ensure protected check-in content was not rendered
            composeTestRule.onAllNodesWithText("Daily Check-In", substring = true).assertCountEquals(0)
        }
    }

    @Test
    fun test02_qrExportDialogSecurity() {
        // Step 2: Authenticate and open Summary QR & Export options dialog
        ScreenshotHelper.step("22", "02", device) {
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

            // Launch fresh MainActivity so AppNavGraph starts at Dashboard for authenticated user
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            composeTestRule.waitForIdle()

            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithContentDescription("Print Summary QR").fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }

            try {
                composeTestRule.onNodeWithContentDescription("Print Summary QR").performClick()
            } catch (_: Exception) {
                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                    composeTestRule.waitForIdle()
                    composeTestRule.onNodeWithContentDescription("Print Summary QR").performClick()
                } catch (_: Exception) {}
            }
            composeTestRule.waitForIdle()

            // Verify Summary QR dialog and export options are displayed
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule.onAllNodesWithText("Health Summary QR").fetchSemanticsNodes().isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }
            composeTestRule.onNodeWithText("Health Summary QR").assertIsDisplayed()
            composeTestRule.onNodeWithContentDescription("Automatically Generated Summary QR Code").assertIsDisplayed()
            composeTestRule.onNodeWithText("Save as PDF").assertIsDisplayed()

            // Test export action
            composeTestRule.onNodeWithText("Save as PDF").performClick()
            composeTestRule.waitForIdle()

            // Close dialog
            try {
                composeTestRule.onNodeWithContentDescription("Close").performClick()
            } catch (_: Throwable) {
                device.pressBack()
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun test03_componentExportAudit() {
        // Step 3: Assert ReminderReceiver is NOT exported (Incident 4 resolution)
        ScreenshotHelper.step("22", "03", device) {
            val receiverInfo = context.packageManager.getReceiverInfo(
                android.content.ComponentName(context, "cit.edu.primera.core.notification.ReminderReceiver"),
                PackageManager.GET_META_DATA
            )
            assertFalse("ReminderReceiver must not be exported (exported=false)", receiverInfo.exported)
            composeTestRule.waitForIdle()
        }
    }
}
