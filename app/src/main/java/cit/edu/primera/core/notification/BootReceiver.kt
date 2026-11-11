
package cit.edu.primera.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Preserve existing daily reminders.
            ReminderManager(context)
                .scheduleDailyReminders()

            // Restore future prenatal appointment reminders.
            try {
                AppointmentReminderScheduler(context)
                    .restoreFromCache()
            } catch (e: Exception) {
                Log.e(
                    "BootReceiver",
                    "Unable to restore appointment reminders",
                    e
                )
            }
        }
    }
}
