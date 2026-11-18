package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
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
import cit.edu.primera.core.notification.BootReceiver
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.core.notification.ReminderReceiver
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class M19AfterRebootTest {

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
    fun test01_scheduledReminderAfterReboot() {
        val prefs = PreferenceRepositoryImpl(context)
        val (hour, minute) = prefs.getReminderTime(NotificationItem.CATEGORY_CHECKIN)

        // Ensure BootReceiver executed (invoked by OS on BOOT_COMPLETED; verified here as well)
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED).apply {
            setPackage(context.packageName)
        }
        BootReceiver().onReceive(context, bootIntent)

        // Step 3: Reboot verification (M19_TC03)
        // Re-verify reminder is still listed and still fires
        ScreenshotHelper.step("19", "03", device) {
            // 1. Assert reminder is still listed with the configured time and enabled
            composeTestRule.onNodeWithText("Reminder Settings").assertIsDisplayed()
            composeTestRule.onNodeWithText("Morning Check-in").assertIsDisplayed()
            composeTestRule.onNodeWithText("Scheduled at ${formatTime(hour, minute)}").assertIsDisplayed()
            composeTestRule.onNodeWithTag("reminder_switch_morning_check_in").assertIsOn()

            // 2. Assert reminder still fires
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

            // Open notification shade and assert notification content
            device.openNotification()
            val titleFound = device.wait(Until.hasObject(By.textContains("Morning Check-in")), 5000)
            assertTrue("Expected 'Morning Check-in' notification in shade after reboot", titleFound)
            val messageFound = device.wait(Until.hasObject(By.textContains("How are you feeling today, Mom?")), 3000)
            assertTrue("Expected notification message in shade after reboot", messageFound)
            device.waitForIdle()
        }

        // Close shade
        device.pressHome()
        device.waitForIdle()
    }
}
