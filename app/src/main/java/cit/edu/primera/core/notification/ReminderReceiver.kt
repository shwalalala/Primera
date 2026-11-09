package cit.edu.primera.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cit.edu.primera.PrimeraApplication
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your daily check-in!"
        val notificationId = intent.getIntOfExtra("notificationId", 1)
        val category = intent.getStringExtra("category") ?: NotificationItem.CATEGORY_SYSTEM
        val destinationRoute = intent.getStringExtra("destinationRoute")

        android.util.Log.d("ReminderReceiver", "Broadcast received: title=$title, id=$notificationId, category=$category")

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            destinationRoute = destinationRoute
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContainer = (context.applicationContext as? PrimeraApplication)?.container
                val notificationRepo = appContainer?.notificationRepository
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

                val item = NotificationItem(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    category = category,
                    destinationRoute = destinationRoute,
                    isRead = false
                )
                notificationRepo?.addNotification(userId, item)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun Intent.getIntOfExtra(name: String, defaultValue: Int): Int {
        return if (hasExtra(name)) getIntExtra(name, defaultValue) else defaultValue
    }
}
