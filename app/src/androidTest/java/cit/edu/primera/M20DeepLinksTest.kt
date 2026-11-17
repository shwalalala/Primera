package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
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
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.core.notification.NotificationHelper
import cit.edu.primera.feature.notification.data.NotificationRepositoryImpl
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M20DeepLinksTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private var originalIntent: Intent? = null

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
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        originalIntent = composeTestRule.activity.intent
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Pre-seed test notification with destinationRoute
        runBlocking {
            val repo = NotificationRepositoryImpl(context)
            repo.clearAllNotifications(uid)
            repo.addNotification(
                uid,
                NotificationItem(
                    id = "m20_test_notif",
                    title = "Check-in Reminder",
                    message = "Time for your daily log",
                    timestamp = System.currentTimeMillis(),
                    category = NotificationItem.CATEGORY_CHECKIN,
                    destinationRoute = Routes.DAILY_CHECKIN,
                    isRead = false
                )
            )
        }

        // Wait for Splash screen to transition to Dashboard
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
    }

    @After
    fun tearDown() {
        originalIntent?.let { orig ->
            composeTestRule.activity.runOnUiThread {
                composeTestRule.activity.intent = orig
            }
        }
    }

    private fun dispatchNotificationRoute(route: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, route)
        }
        composeTestRule.activity.runOnUiThread {
            try {
                val orig = composeTestRule.activity.intent
                val method = MainActivity::class.java.getDeclaredMethod("onNewIntent", Intent::class.java)
                method.isAccessible = true
                method.invoke(composeTestRule.activity, intent)
                // Restore original intent so ActivityScenario can track lifecycle destruction
                composeTestRule.activity.intent = orig
            } catch (e: Exception) {
                android.util.Log.e("M20DeepLinksTest", "Failed to dispatch onNewIntent", e)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testDeepLinksFullSuite() {
        // Step 1: In-app notification tap routes to correct screen
        ScreenshotHelper.step("20", "01", device) {
            // Open sidebar
            composeTestRule.onAllNodes(
                hasClickAction() and hasAnyAncestor(hasTestTag("dashboard_top_bar"))
            ).onFirst().performClick()
            composeTestRule.waitForIdle()

            // Navigate to Notifications
            composeTestRule.onNodeWithContentDescription("Notifications").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.waitUntil(8000) {
                composeTestRule.onAllNodesWithText("Check-in Reminder").fetchSemanticsNodes().isNotEmpty()
            }
            // Click notification item to open preview dialog
            composeTestRule.onNodeWithText("Check-in Reminder").performClick()
            composeTestRule.waitForIdle()

            // Click destination action button in details dialog
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("Open Daily Check-In").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Open Daily Check-In").performClick()
            composeTestRule.waitForIdle()

            // Verify routed to Daily Check-in screen
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Daily Check-In").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Symptoms").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitForIdle()
        }

        // Step 2: System notification intent routes to Smartwatch screen ("device")
        ScreenshotHelper.step("20", "02", device) {
            dispatchNotificationRoute(Routes.DEVICE)

            // Verify routed to Smartwatch screen
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Smartwatch").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Connect").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Sync").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitForIdle()
        }

        // Step 3: System notification intent routes to Profile screen ("profile")
        ScreenshotHelper.step("20", "03", device) {
            dispatchNotificationRoute(Routes.PROFILE)

            // Verify routed to Profile screen
            composeTestRule.waitUntil(10000) {
                composeTestRule.onAllNodesWithText("Profile").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("My Profile").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitForIdle()
        }
    }
}
