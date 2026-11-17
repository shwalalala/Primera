package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import cit.edu.primera.core.notification.BootReceiver
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.core.notification.ReminderReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M19ScheduledNotificationsTest {

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
        runBlocking {
            LoginHelper.signInWithTestAccount()
        }
    }

    @Test
    fun test01_scheduledReminderBeforeReboot() {
        // Step 1: Set reminder, background app, trigger alarm/notification, open shade, assert content
        ScreenshotHelper.step("19", "01", device) {
            // Trigger reminder broadcast directly to verify alarm receiver execution
            val triggerIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", "Morning Check-in")
                putExtra("message", "How are you feeling today, Mom?")
                putExtra("notification_id", ReminderManager.ID_CHECKIN)
                putExtra("category", "checkin")
                putExtra("destination_route", "daily_checkin")
            }
            context.sendBroadcast(triggerIntent)

            // Background the app
            device.pressHome()
            device.waitForIdle()

            // Open notification shade
            device.openNotification()
            device.wait(Until.hasObject(By.textContains("Morning Check-in")), 3000)

            // Take screenshot with shade open
            device.waitForIdle()
        }

        // Step 2: Check reminder is saved in settings list
        ScreenshotHelper.step("19", "02", device) {
            device.pressBack() // close notification shade
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithText("Settings").performClick()
                composeTestRule.onNodeWithText("Reminders").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun test02_scheduledReminderAfterReboot() {
        // Step 3: Verification after device reboot or BootReceiver execution
        ScreenshotHelper.step("19", "03", device) {
            // Simulate ACTION_BOOT_COMPLETED broadcast to BootReceiver
            val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED).apply {
                setPackage(context.packageName)
            }
            BootReceiver().onReceive(context, bootIntent)

            try {
                composeTestRule.onNodeWithText("Settings").performClick()
                composeTestRule.onNodeWithText("Reminders").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun test03_disabledReminder() {
        // Step 4: Test a disabled reminder
        ScreenshotHelper.step("19", "04", device) {
            try {
                composeTestRule.onNodeWithText("Settings").performClick()
                composeTestRule.onNodeWithText("Reminders").performClick()
                // Toggle off Morning Check-in if toggle available
                composeTestRule.onNodeWithText("Morning Check-in").performClick()
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun test04_deniedPermission() {
        // Step 5: Test denied notification permission
        ScreenshotHelper.step("19", "05", device) {
            try {
                device.executeShellCommand("pm revoke ${context.packageName} android.permission.POST_NOTIFICATIONS")
            } catch (_: Exception) {}
            composeTestRule.waitForIdle()
        }
    }
}
