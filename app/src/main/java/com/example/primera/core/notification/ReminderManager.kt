package com.example.primera.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyReminders() {
        // Morning Check-in at 9:00 AM
        scheduleReminder(9, 0, "Morning Check-in", "How are you feeling today, Mom?", 1001)
        
        // Hydration Reminder at 2:00 PM
        scheduleReminder(14, 0, "Stay Hydrated", "Remember to drink water for you and baby!", 1002)
        
        // Evening Wind-down at 8:00 PM
        scheduleReminder(20, 0, "Evening Wind-down", "Time to log your symptoms and rest.", 1003)
    }

    private fun scheduleReminder(hour: Int, minute: Int, title: String, message: String, notificationId: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
