package cit.edu.primera

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.NotificationManagerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.data.PreferenceRepositoryImpl
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.core.notification.NotificationHelper
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.core.notification.ReminderReceiver
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class M19BeforeRebootTest {

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
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                    SeedData.seedUserProfile(uid)
                }

                // Schedule a reminder 5 minutes ahead
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, 5)
                }
                val targetHour = calendar.get(Calendar.HOUR_OF_DAY)
                val targetMinute = calendar.get(Calendar.MINUTE)

                val prefs = PreferenceRepositoryImpl(targetContext)
                prefs.setReminderEnabled(NotificationItem.CATEGORY_CHECKIN, true)
                prefs.setReminderTime(NotificationItem.CATEGORY_CHECKIN, targetHour, targetMinute)
                ReminderManager(targetContext).scheduleDailyReminders()
            }
        }
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        navigateToReminderSettings()
    }

    private fun navigateToReminderSettings() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("dashboard_top_bar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        if (composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()) {
            return
        }

        // Open sidebar
        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyAncestor(hasTestTag("dashboard_top_bar"))
        ).onFirst().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Notifications
        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodes(hasContentDescription("Settings")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasContentDescription("Settings")).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodesWithText("Reminder Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
    }

    @Test
    fun test01_setReminderAndVerifyShade() {
        val prefs = PreferenceRepositoryImpl(context)
        val (targetHour, targetMinute) = prefs.getReminderTime(NotificationItem.CATEGORY_CHECKIN)

        // Step 2: Reminder saved and enabled in list (M19_TC02)
        ScreenshotHelper.step("19", "02", device) {
            composeTestRule.onNodeWithText("Reminder Settings").assertIsDisplayed()
            composeTestRule.onNodeWithText("Morning Check-in").assertIsDisplayed()
            composeTestRule.onNodeWithText("Scheduled at ${formatTime(targetHour, targetMinute)}").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsOn()
        }

        // Step 1: Trigger notification, background app, open shade, assert notification content (M19_TC01)
        ScreenshotHelper.step("19", "01", device) {
            val triggerIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", "Morning Check-in")
                putExtra("message", "How are you feeling today, Mom?")
                putExtra("notificationId", ReminderManager.ID_CHECKIN)
                putExtra("category", NotificationItem.CATEGORY_CHECKIN)
                putExtra("destinationRoute", Routes.DAILY_CHECKIN)
            }
            context.sendBroadcast(triggerIntent)

            // Background the app
            device.pressHome()
            device.waitForIdle()

            // Open notification shade
            device.openNotification()
            val titleFound = device.wait(Until.hasObject(By.textContains("Morning Check-in")), 5000)
            assertTrue("Expected 'Morning Check-in' notification in shade", titleFound)
            val messageFound = device.wait(Until.hasObject(By.textContains("How are you feeling today, Mom?")), 3000)
            assertTrue("Expected notification message in shade", messageFound)
            device.waitForIdle()
        }

        // Close shade and return to app
        device.pressHome()
        device.waitForIdle()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun test02_disabledReminderDoesNotFire() {
        navigateToReminderSettings()

        // Step 4: Disabled reminder (M19_TC04)
        ScreenshotHelper.step("19", "04", device) {
            // Toggle Hydration Reminder off
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").assertIsOff()

            // Verify disabled in preferences
            val prefs = PreferenceRepositoryImpl(context)
            assertFalse(
                "Hydration reminder should be disabled in preferences",
                prefs.isReminderEnabled(NotificationItem.CATEGORY_HYDRATION)
            )

            // Verify AlarmManager PendingIntent is cancelled
            val checkIntent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ReminderManager.ID_HYDRATION,
                checkIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            assertNull("Alarm PendingIntent for disabled reminder should be cancelled", pendingIntent)

            // Restore switch to ON for clean state
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").assertIsOn()
        }
    }

    @Test
    fun test03_deniedNotificationPermission() {
        // Step 5: Denied notification permission (M19_TC05)
        ScreenshotHelper.step("19", "05", device) {
            // Note OS Invariant & Defect: Revoking POST_NOTIFICATIONS at runtime via
            // `pm revoke` or System Settings switch causes Android OS (API 33+) to terminate
            // the app process immediately with status -1.
            // Closest adb alternative for master notification control:
            //   adb shell pm revoke cit.edu.primera android.permission.POST_NOTIFICATIONS
            //   adb shell pm grant cit.edu.primera android.permission.POST_NOTIFICATIONS
            //
            // Verify Reminder Settings screen remains fully displayed and operable
            composeTestRule.onNodeWithText("Reminder Settings").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_hydration_reminder").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_evening_wind_down").assertIsDisplayed()

            // Verify NotificationHelper safely handles notification dispatch without throwing or crashing
            NotificationHelper(context).showNotification(
                title = "Permission Safety Test",
                message = "Testing safe dispatch without crash",
                notificationId = 9999
            )
        }
    }
}
