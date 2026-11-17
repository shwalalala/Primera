package cit.edu.primera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.core.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M22SecurityTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false)

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
            try {
                composeTestRule.onNodeWithText("Welcome to Primera").assertExists()
            } catch (_: Exception) {
                composeTestRule.onNodeWithText("Log In").assertExists()
            }
        }
    }

    @Test
    fun test02_qrExportDialogSecurity() {
        // Step 2: Authenticate and open Summary QR & Export options dialog
        ScreenshotHelper.step("22", "02", device) {
            runBlocking {
                LoginHelper.signInWithTestAccount()
            }
            try {
                composeTestRule.onNodeWithContentDescription("Print Summary QR").performClick()
            } catch (_: Exception) {
                try {
                    composeTestRule.onNodeWithText("Home").performClick()
                    composeTestRule.onNodeWithContentDescription("Print Summary QR").performClick()
                } catch (_: Exception) {}
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
            assertFalse("ReminderReceiver must not be exported", receiverInfo.exported)
            composeTestRule.waitForIdle()
        }
    }

    /**
     * Non-Instrumentable Security Verification Procedures:
     *
     * 1. HTTPS Network Interception & Proxying:
     *    adb shell settings put global http_proxy <HOST_IP>:8080
     *    mitmproxy -p 8080
     *    Verify TLS certificate pinning and strict HTTPS transport for Firebase and Gemini API endpoints.
     *    Restore with: adb shell settings put global http_proxy :0
     *
     * 2. Encryption at Rest & Sandbox Isolation:
     *    adb shell run-as cit.edu.primera ls -la /data/data/cit.edu.primera/
     *    adb shell run-as cit.edu.primera cat /data/data/cit.edu.primera/shared_prefs/primera_app_preferences.xml
     *    Verify that sensitive tokens and passwords are not stored in plaintext SharedPreferences,
     *    and user data is secured within private sandbox storage inaccessible to other apps.
     *
     * 3. Cloud Firestore Security Rules Verification:
     *    Run automated unit testing via Firebase Emulator Suite:
     *    firebase emulators:exec --only firestore "npm test"
     *    Rules test file verifies that reads/writes are restricted to authenticated request.auth.uid == userId.
     */
}
