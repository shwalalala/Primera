
package cit.edu.primera.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.feature.checkins.data.PrenatalRecordDto
import cit.edu.primera.feature.notification.domain.NotificationItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AppointmentReminderScheduler(
    context: Context
) {
    private val appContext = context.applicationContext

    private val alarmManager =
        appContext.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    private val prefs =
        appContext.getSharedPreferences(
            "primera_appointment_reminders",
            Context.MODE_PRIVATE
        )

    private data class ReminderEntry(
        val userId: String,
        val recordId: String,
        val kind: String,
        val triggerAt: Long,
        val appointmentAt: Long
    )

    companion object {
        private const val TAG = "AppointmentReminders"
        private const val KEY_ENTRIES = "scheduled_entries"

        private const val KIND_DAY_BEFORE = "day_before"
        private const val KIND_HOUR_BEFORE = "hour_before"
    }

    // ---------------------------------------------
    // SYNCHRONIZE WITH FIRESTORE RECORDS
    // ---------------------------------------------

    @Synchronized
    fun synchronize(
        userId: String,
        records: List<PrenatalRecordDto>
    ) {
        if (userId.isBlank()) return

        val oldEntries = readEntries()

        val now = System.currentTimeMillis()
        val desiredEntries = mutableListOf<ReminderEntry>()

        records
            .filter { it.status == "scheduled" }
            .forEach { record ->

                val recordId = record.id
                    ?.takeIf { it.isNotBlank() }
                    ?: return@forEach

                val time = record.details["time"]
                    ?.takeIf { it.isNotBlank() }
                    ?: return@forEach

                val appointmentAt = parseAppointment(
                    record.visitDate,
                    time
                ) ?: run {
                    Log.w(
                        TAG,
                        "Invalid appointment date/time for $recordId"
                    )
                    return@forEach
                }

                if (appointmentAt <= now) {
                    return@forEach
                }

                val dayBefore =
                    appointmentAt - TimeUnit.DAYS.toMillis(1)

                val hourBefore =
                    appointmentAt - TimeUnit.HOURS.toMillis(1)

                if (dayBefore > now) {
                    desiredEntries.add(
                        ReminderEntry(
                            userId = userId,
                            recordId = recordId,
                            kind = KIND_DAY_BEFORE,
                            triggerAt = dayBefore,
                            appointmentAt = appointmentAt
                        )
                    )
                }

                if (hourBefore > now) {
                    desiredEntries.add(
                        ReminderEntry(
                            userId = userId,
                            recordId = recordId,
                            kind = KIND_HOUR_BEFORE,
                            triggerAt = hourBefore,
                            appointmentAt = appointmentAt
                        )
                    )
                }
            }

        // Cancel old alarms, including those belonging
        // to another previously signed-in account.
        oldEntries.forEach { cancelAlarm(it) }

        // Schedule only the currently signed-in user's
        // upcoming prenatal appointments.
        desiredEntries.forEach { scheduleAlarm(it) }

        saveEntries(desiredEntries)

        Log.d(
            TAG,
            "Synchronized ${desiredEntries.size} " +
                    "appointment reminders for user $userId"
        )
    }

    // ---------------------------------------------
    // RESTORE AFTER PHONE RESTART / APP UPDATE
    // ---------------------------------------------

    @Synchronized
    fun restoreFromCache() {
        val now = System.currentTimeMillis()

        val entries = readEntries().filter {
            it.triggerAt > now &&
                    it.appointmentAt > now
        }

        entries.forEach { scheduleAlarm(it) }

        saveEntries(entries)

        Log.d(
            TAG,
            "Restored ${entries.size} cached appointment reminders"
        )
    }

    // ---------------------------------------------
    // SCHEDULE A ONE-TIME ALARM
    // ---------------------------------------------

    private fun scheduleAlarm(entry: ReminderEntry) {
        val pendingIntent = pendingIntentFor(
            entry,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    entry.triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    entry.triggerAt,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Unable to schedule appointment reminder",
                e
            )
        }
    }

    // ---------------------------------------------
    // CANCEL AN EXISTING ALARM
    // ---------------------------------------------

    private fun cancelAlarm(entry: ReminderEntry) {
        val pendingIntent = pendingIntentFor(
            entry,
            PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // ---------------------------------------------
    // BUILD A UNIQUE PENDING INTENT
    // ---------------------------------------------

    private fun pendingIntentFor(
        entry: ReminderEntry,
        flags: Int
    ): PendingIntent? {

        val identity =
            "${entry.userId}|${entry.recordId}|${entry.kind}"

        val notificationId =
            ("appointment|$identity".hashCode() and 0x7fffffff)

        val appointmentDisplay =
            SimpleDateFormat(
                "MMM d, yyyy 'at' h:mm a",
                Locale.US
            ).format(Date(entry.appointmentAt))

        val title: String
        val message: String

        if (entry.kind == KIND_DAY_BEFORE) {
            title = "Prenatal Appointment Tomorrow"
            message =
                "You have a prenatal appointment tomorrow, " +
                        appointmentDisplay + "."
        } else {
            title = "Upcoming Prenatal Appointment"
            message =
                "Your prenatal appointment is in about one hour, " +
                        appointmentDisplay + "."
        }

        val intent = Intent(
            appContext,
            ReminderReceiver::class.java
        ).apply {
            action = "cit.edu.primera.APPOINTMENT_REMINDER"

            data = Uri.Builder()
                .scheme("primera")
                .authority("appointment-reminder")
                .appendPath(entry.userId)
                .appendPath(entry.recordId)
                .appendPath(entry.kind)
                .build()

            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
            putExtra(
                "category",
                NotificationItem.CATEGORY_APPOINTMENT
            )
            putExtra(
                "destinationRoute",
                Routes.CHECKIN
            )

            // Used by ReminderReceiver to prevent
            // delivery to another signed-in account.
            putExtra(
                "appointmentUserId",
                entry.userId
            )
        }

        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            flags
        )
    }

    // ---------------------------------------------
    // DATE / TIME PARSING
    // ---------------------------------------------

    private fun parseAppointment(
        visitDate: String,
        time: String
    ): Long? {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd h:mm a",
            Locale.US
        ).apply {
            isLenient = false
        }

        return runCatching {
            formatter.parse(
                "$visitDate ${time.trim()}"
            )?.time
        }.getOrNull()
    }

    // ---------------------------------------------
    // LOCAL CACHE
    // ---------------------------------------------

    private fun readEntries(): List<ReminderEntry> {
        val raw = prefs.getString(
            KEY_ENTRIES,
            null
        ) ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            val result = mutableListOf<ReminderEntry>()

            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                result.add(
                    ReminderEntry(
                        userId = item.getString("userId"),
                        recordId = item.getString("recordId"),
                        kind = item.getString("kind"),
                        triggerAt = item.getLong("triggerAt"),
                        appointmentAt =
                            item.getLong("appointmentAt")
                    )
                )
            }

            result
        }.getOrElse {
            Log.e(
                TAG,
                "Unable to read cached appointment reminders",
                it
            )
            emptyList()
        }
    }

    private fun saveEntries(
        entries: List<ReminderEntry>
    ) {
        val array = JSONArray()

        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("userId", entry.userId)
                    put("recordId", entry.recordId)
                    put("kind", entry.kind)
                    put("triggerAt", entry.triggerAt)
                    put("appointmentAt", entry.appointmentAt)
                }
            )
        }

        prefs.edit()
            .putString(
                KEY_ENTRIES,
                array.toString()
            )
            .apply()
    }
}
