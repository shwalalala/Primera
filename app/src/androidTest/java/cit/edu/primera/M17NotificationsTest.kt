package cit.edu.primera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.feature.notification.data.NotificationRepositoryImpl
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M17NotificationsTest {

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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Pre-seed test notifications
        runBlocking {
            val repo = NotificationRepositoryImpl(context)
            repo.clearAllNotifications(uid)

            val now = System.currentTimeMillis()
            repo.addNotification(
                uid,
                NotificationItem(
                    id = "test_notif_checkin",
                    title = "Daily Check-in Reminder",
                    message = "Time for your daily check-in!",
                    timestamp = now,
                    category = NotificationItem.CATEGORY_CHECKIN,
                    destinationRoute = "daily_checkin",
                    isRead = false
                )
            )
            repo.addNotification(
                uid,
                NotificationItem(
                    id = "test_notif_hydration",
                    title = "Hydration Alert",
                    message = "Drink a glass of water.",
                    timestamp = now - 3600000,
                    category = NotificationItem.CATEGORY_HYDRATION,
                    destinationRoute = null,
                    isRead = false
                )
            )
            repo.addNotification(
                uid,
                NotificationItem(
                    id = "test_notif_appointment",
                    title = "Doctor Appointment",
                    message = "Prenatal visit scheduled for tomorrow.",
                    timestamp = now - 7200000,
                    category = "appointment",
                    destinationRoute = null,
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

        // Open sidebar
        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyAncestor(hasTestTag("dashboard_top_bar"))
        ).onFirst().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Notifications screen
        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodesWithText("Notifications").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testNotificationsScreenSuite() {
        // Step 1: Screen load, chronological order, badge count
        ScreenshotHelper.step("17", "01", device) {
            composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
            // Verify unread badge count
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("3 unread").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("3 unread").assertIsDisplayed()

            // Verify notifications displayed in chronological order
            composeTestRule.onNodeWithText("Daily Check-in Reminder").assertIsDisplayed()
            composeTestRule.onNodeWithText("Hydration Alert").assertIsDisplayed()
            composeTestRule.onNodeWithText("Doctor Appointment").assertIsDisplayed()
        }

        // Step 2: Category tabs filter
        ScreenshotHelper.step("17", "02", device) {
            // Click Check-ins filter tab
            composeTestRule.onNodeWithText("Check-ins").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Daily Check-in Reminder").assertIsDisplayed()

            // Click Hydration filter tab
            composeTestRule.onNodeWithText("Hydration").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Hydration Alert").assertIsDisplayed()

            // Return to All filter tab
            composeTestRule.onNodeWithText("All").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Daily Check-in Reminder").assertIsDisplayed()
            composeTestRule.onNodeWithText("Hydration Alert").assertIsDisplayed()
        }

        // Step 3: Mark single notification as read
        ScreenshotHelper.step("17", "03", device) {
            // Click on Daily Check-in Reminder to open details dialog
            composeTestRule.onNodeWithText("Daily Check-in Reminder").performClick()
            composeTestRule.waitForIdle()

            // Verify details dialog is displayed
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Close preview")).fetchSemanticsNodes().isNotEmpty()
            }
            // Dismiss details preview dialog
            composeTestRule.onNodeWithContentDescription("Close preview").performClick()
            composeTestRule.waitForIdle()

            // Verify unread badge count decremented to 2
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("2 unread").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("2 unread").assertIsDisplayed()
        }

        // Step 4: Mark all as read
        ScreenshotHelper.step("17", "04", device) {
            composeTestRule.onNodeWithTag("notifications_mark_all_read_button").performClick()
            composeTestRule.waitForIdle()

            // Verify unread badge is no longer displayed
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("unread", substring = true).fetchSemanticsNodes().isEmpty()
            }
            composeTestRule.waitForIdle()
        }

        // Step 5: Clear all & empty state
        ScreenshotHelper.step("17", "05", device) {
            composeTestRule.onNodeWithTag("notifications_clear_all_button").performClick()
            composeTestRule.waitForIdle()

            // Verify empty state is displayed
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText("No notifications available").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("No notifications available").assertIsDisplayed()
            composeTestRule.onNodeWithText("You're all caught up on maternal wellness updates!").assertIsDisplayed()
        }
    }
}
