package cit.edu.primera.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cit.edu.primera.core.data.PreferenceRepositoryImpl
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.feature.notification.domain.NotificationItem
import java.util.Calendar

open class ReminderManager(private val context: Context? = null) {

    private val alarmManager by lazy {
        context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    }
    private val preferenceRepository by lazy {
        context?.let { PreferenceRepositoryImpl(it) }
    }

    companion object {
        const val ID_CHECKIN = 1001
        const val ID_HYDRATION = 1002
        const val ID_WELLNESS = 1003
    }

    open fun scheduleDailyReminders() {
        val prefs = preferenceRepository ?: return

        // Morning Check-in
        if (prefs.isReminderEnabled(NotificationItem.CATEGORY_CHECKIN)) {
            val (hour, minute) = prefs.getReminderTime(NotificationItem.CATEGORY_CHECKIN)
            scheduleReminder(
                hour,
                minute,
                "Morning Check-in",
                "How are you feeling today, Mom?",
                ID_CHECKIN,
                NotificationItem.CATEGORY_CHECKIN,
                Routes.DAILY_CHECKIN
            )
        } else {
            cancelReminder(ID_CHECKIN)
        }

        // Hydration Reminder
        if (prefs.isReminderEnabled(NotificationItem.CATEGORY_HYDRATION)) {
            val (hour, minute) = prefs.getReminderTime(NotificationItem.CATEGORY_HYDRATION)
            scheduleReminder(
                hour,
                minute,
                "Stay Hydrated",
                "Remember to drink water for you and baby!",
                ID_HYDRATION,
                NotificationItem.CATEGORY_HYDRATION,
                Routes.INSIGHT
            )
        } else {
            cancelReminder(ID_HYDRATION)
        }

        // Evening Wind-down
        if (prefs.isReminderEnabled(NotificationItem.CATEGORY_WELLNESS)) {
            val (hour, minute) = prefs.getReminderTime(NotificationItem.CATEGORY_WELLNESS)
            scheduleReminder(
                hour,
                minute,
                "Evening Wind-down",
                "Time to log your symptoms and rest.",
                ID_WELLNESS,
                NotificationItem.CATEGORY_WELLNESS,
                Routes.CHECKIN
            )
        } else {
            cancelReminder(ID_WELLNESS)
        }
    }

    private fun scheduleReminder(
        hour: Int,
        minute: Int,
        title: String,
        message: String,
        notificationId: Int,
        category: String,
        destinationRoute: String
    ) {
        val ctx = context ?: return
        val am = alarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
            putExtra("category", category)
            putExtra("destinationRoute", destinationRoute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    open fun cancelReminder(notificationId: Int) {
        val ctx = context ?: return
        val am = alarmManager ?: return

        val intent = Intent(ctx, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            am.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
