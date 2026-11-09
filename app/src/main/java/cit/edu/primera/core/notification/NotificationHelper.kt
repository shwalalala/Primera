package cit.edu.primera.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cit.edu.primera.R

@SuppressLint("NotificationPermission", "MissingPermission")
class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val REMINDER_CHANNEL_ID = "primera_reminders_channel_v3"
        const val REMINDER_CHANNEL_NAME = "Primera Reminders"
        const val EXTRA_DESTINATION_ROUTE = "extra_destination_route"
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for daily check-in and goal reminders"
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created: $REMINDER_CHANNEL_ID")
    }

    @SuppressLint("MissingPermission")
    fun showNotification(
        title: String,
        message: String,
        notificationId: Int,
        destinationRoute: String? = null
    ) {
        val intent = android.content.Intent(context, cit.edu.primera.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            destinationRoute?.let { putExtra(EXTRA_DESTINATION_ROUTE, it) }
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_logo)
        } catch (_: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        Log.d(TAG, "showNotification: id=$notificationId, title=$title, hasPermission=$hasPermission")

        if (hasPermission) {
            try {
                notificationManager.notify(notificationId, builder.build())
                Log.d(TAG, "Successfully called notificationManager.notify($notificationId)")
            } catch (e: Exception) {
                Log.e(TAG, "Error posting notification", e)
            }
        } else {
            Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted")
        }
    }
}
