package com.example.primera.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your daily check-in!"
        val notificationId = intent.getIntOfExtra("notificationId", 1)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message, notificationId)
    }

    private fun Intent.getIntOfExtra(name: String, defaultValue: Int): Int {
        return if (hasExtra(name)) getIntExtra(name, defaultValue) else defaultValue
    }
}
