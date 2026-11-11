
package cit.edu.primera.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import cit.edu.primera.PrimeraApplication
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val title =
            intent.getStringExtra("title") ?: "Reminder"

        val message =
            intent.getStringExtra("message")
                ?: "Time for your daily check-in!"

        val notificationId =
            intent.getIntExtra("notificationId", 1)

        val category =
            intent.getStringExtra("category")
                ?: NotificationItem.CATEGORY_SYSTEM

        val destinationRoute =
            intent.getStringExtra("destinationRoute")

        val currentUserId =
            FirebaseAuth.getInstance().currentUser?.uid

        // Only appointment reminders contain this extra.
        // Prevent reminders belonging to a different
        // Firebase account from being displayed.
        val appointmentUserId =
            intent.getStringExtra("appointmentUserId")

        if (appointmentUserId != null) {
            if (
                currentUserId == null ||
                currentUserId != appointmentUserId
            ) {
                Log.d(
                    "ReminderReceiver",
                    "Skipped appointment reminder for " +
                            "a different or signed-out user"
                )
                return
            }
        }

        Log.d(
            "ReminderReceiver",
            "Broadcast received: " +
                    "title=$title, " +
                    "id=$notificationId, " +
                    "category=$category"
        )

        val notificationHelper =
            NotificationHelper(context)

        notificationHelper.showNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            destinationRoute = destinationRoute
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContainer =
                    (context.applicationContext
                            as? PrimeraApplication)?.container

                val notificationRepo =
                    appContainer?.notificationRepository

                val userId =
                    currentUserId ?: "guest"

                val item = NotificationItem(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    category = category,
                    destinationRoute = destinationRoute,
                    isRead = false
                )

                notificationRepo?.addNotification(
                    userId,
                    item
                )
            } catch (e: Exception) {
                Log.e(
                    "ReminderReceiver",
                    "Failed to save reminder notification",
                    e
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
