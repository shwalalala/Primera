package cit.edu.primera.core.logging

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Asynchronous, non-blocking validator and logger for system-level evidence collection.
 * Logging operates in background coroutines on Dispatchers.IO to guarantee zero performance overhead on the UI thread.
 * All logs are written to Firestore "system_validation_logs" and "user_activity_metrics" collections using pseudonymous participant codes (e.g., P101).
 */
class SystemValidationLogger(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Generates or retrieves a pseudonymous participant code (e.g. "P101", "P942").
     * Uses a deterministic non-reversible hash of the user ID so no actual PII/PHI is stored in validation logs.
     */
    fun getParticipantCode(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) return "P000"
        val hash = abs(uid.hashCode()) % 900 + 100 // Produces range P100 - P999
        return "P$hash"
    }

    // ==========================================
    // 1. GO1 - SMARTWATCH INTEGRATION LOGGING
    // ==========================================
    fun logSmartwatchSync(
        syncAttempt: Int,
        syncStatus: String, // "successful" or "failed"
        syncDurationMs: Long,
        heartRateSync: String = "successful", // "successful", "failed", "missing", "n/a"
        stepsSync: String = "successful",
        sleepSync: String = "successful",
        dashboardUpdate: String = "successful",
        missingDataDetected: Boolean = false,
        failureReason: String? = null
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mutableMapOf<String, Any?>(
                    "participant_code" to participantCode,
                    "event_type" to "smartwatch_sync",
                    "sync_attempt" to syncAttempt,
                    "sync_status" to syncStatus,
                    "sync_duration_ms" to syncDurationMs,
                    "heart_rate_sync" to heartRateSync,
                    "steps_sync" to stepsSync,
                    "sleep_sync" to sleepSync,
                    "dashboard_update" to dashboardUpdate,
                    "missing_data_detected" to missingDataDetected,
                    "failure_reason" to failureReason,
                    "timestamp" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                firestore.collection("system_validation_logs").add(logData)

                // Update user activity metrics
                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mutableMapOf<String, Any>(
                    "participant_code" to participantCode,
                    "smartwatch_sync_count" to FieldValue.increment(1),
                    "updated_at" to FieldValue.serverTimestamp()
                )
                if (syncStatus == "successful") {
                    updates["smartwatch_successful_sync_count"] = FieldValue.increment(1)
                }
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log smartwatch sync: ${e.message}")
            }
        }
    }

    // ==========================================
    // 2. GO2 - HEALTH CHECK-INS & SPEECH-TO-TEXT LOGGING
    // ==========================================
    fun logVoiceTranscription(
        transcriptionStatus: String, // "successful" or "failed"
        transcriptionDurationMs: Long,
        transcriptReviewed: Boolean = true,
        transcriptEdited: Boolean = false,
        entrySaved: Boolean = true,
        failureReason: String? = null
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mutableMapOf<String, Any?>(
                    "participant_code" to participantCode,
                    "event_type" to "voice_transcription",
                    "transcription_status" to transcriptionStatus,
                    "transcription_duration_ms" to transcriptionDurationMs,
                    "transcript_reviewed" to transcriptReviewed,
                    "transcript_edited" to transcriptEdited,
                    "entry_saved" to entrySaved,
                    "failure_reason" to failureReason,
                    "timestamp" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                firestore.collection("system_validation_logs").add(logData)
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log voice transcription: ${e.message}")
            }
        }
    }

    fun logHealthCheckin(
        inputMethod: String, // "voice", "manual", "hybrid"
        requiredFieldsTotal: Int = 5,
        requiredFieldsCompleted: Int = 5,
        validationPassed: Boolean = true,
        entrySaved: Boolean = true,
        submissionDurationMs: Long = 0L
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "health_checkin",
                    "input_method" to inputMethod,
                    "required_fields_total" to requiredFieldsTotal,
                    "required_fields_completed" to requiredFieldsCompleted,
                    "validation_passed" to validationPassed,
                    "entry_saved" to entrySaved,
                    "submission_duration_ms" to submissionDurationMs,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)

                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mapOf(
                    "participant_code" to participantCode,
                    "maternal_health_logs_count" to FieldValue.increment(1),
                    "updated_at" to FieldValue.serverTimestamp()
                )
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log health checkin: ${e.message}")
            }
        }
    }

    fun logHealthSummary(
        summaryGenerationStatus: String, // "successful" or "failed"
        summaryGenerationDurationMs: Long,
        qrGenerationStatus: String = "n/a", // "successful", "failed", "n/a"
        printGenerationStatus: String = "n/a"
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "health_summary",
                    "summary_generation_status" to summaryGenerationStatus,
                    "summary_generation_duration_ms" to summaryGenerationDurationMs,
                    "qr_generation_status" to qrGenerationStatus,
                    "print_generation_status" to printGenerationStatus,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log health summary: ${e.message}")
            }
        }
    }

    // ==========================================
    // 3. GO3 - WELLNESS GOALS & ENGAGEMENT LOGGING
    // ==========================================
    fun logWellnessGoal(
        goalType: String, // "hydration", "steps", "sleep", "weight", "custom"
        goalCreated: Boolean,
        goalCompleted: Boolean,
        goalPeriod: String = "daily"
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "wellness_goal",
                    "goal_type" to goalType,
                    "goal_created" to goalCreated,
                    "goal_completed" to goalCompleted,
                    "completion_date" to FieldValue.serverTimestamp(),
                    "goal_period" to goalPeriod,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)

                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mutableMapOf<String, Any>(
                    "participant_code" to participantCode,
                    "updated_at" to FieldValue.serverTimestamp()
                )
                if (goalCreated) {
                    updates["daily_goals_assigned_count"] = FieldValue.increment(1)
                }
                if (goalCompleted) {
                    updates["daily_goals_completed_count"] = FieldValue.increment(1)
                }
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log wellness goal: ${e.message}")
            }
        }
    }

    fun logMaternalHealthLog(
        logType: String, // "symptom_checkin", "prenatal_checkup", "weight_log", etc.
        logCompleted: Boolean = true
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "maternal_health_log",
                    "log_type" to logType,
                    "log_completed" to logCompleted,
                    "log_date" to FieldValue.serverTimestamp(),
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)

                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mutableMapOf<String, Any>(
                    "participant_code" to participantCode,
                    "maternal_health_logs_count" to FieldValue.increment(1),
                    "updated_at" to FieldValue.serverTimestamp()
                )
                if (logType.contains("weight", ignoreCase = true)) {
                    updates["weight_logs_count"] = FieldValue.increment(1)
                }
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(
                    "SystemValidationLogger",
                    "Failed to log maternal health activity: ${e.message}"
                )
            }
        }
    }

    fun logAppActivity(
        activityType: String // "dashboard_view", "login", "app_open"
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "app_activity",
                    "activity_type" to activityType,
                    "activity_date" to FieldValue.serverTimestamp(),
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)

                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val cal = Calendar.getInstance()
                val weekStr = "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"

                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mutableMapOf<String, Any>(
                    "participant_code" to participantCode,
                    "last_login_timestamp" to FieldValue.serverTimestamp(),
                    "daily_login_dates" to FieldValue.arrayUnion(dateStr),
                    "weekly_active_weeks" to FieldValue.arrayUnion(weekStr),
                    "updated_at" to FieldValue.serverTimestamp()
                )
                if (activityType == "login") {
                    updates["total_logins"] = FieldValue.increment(1)
                }
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log app activity: ${e.message}")
            }
        }
    }

    fun logReminderEvent(
        reminderType: String, // "medication", "appointment", "checkin", "milestone"
        notificationSent: Boolean,
        notificationStatus: String, // "delivered", "scheduled", "failed"
        markedCompleted: Boolean = false
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mapOf(
                    "participant_code" to participantCode,
                    "event_type" to "reminder",
                    "reminder_type" to reminderType,
                    "notification_sent" to notificationSent,
                    "notification_status" to notificationStatus,
                    "marked_completed" to markedCompleted,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("system_validation_logs").add(logData)

                val metricsRef =
                    firestore.collection("user_activity_metrics").document(participantCode)
                val updates = mutableMapOf<String, Any>(
                    "participant_code" to participantCode,
                    "updated_at" to FieldValue.serverTimestamp()
                )
                if (notificationSent) {
                    updates["reminders_sent_count"] = FieldValue.increment(1)
                }
                if (markedCompleted) {
                    updates["reminders_completed_count"] = FieldValue.increment(1)
                }
                metricsRef.set(updates, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log reminder event: ${e.message}")
            }
        }
    }

    // ==========================================
    // 4. OVERALL APPLICATION PERFORMANCE LOGGING
    // ==========================================
    fun logPerformanceEvent(
        eventType: String, // "login", "registration", "dashboard_loading", "health_record_retrieval", "health_record_saving", "qr_generation", "print_generation", "application_error", "data_sync"
        status: String, // "successful" or "failed"
        durationMs: Long? = null,
        failureReason: String? = null,
        errorDetails: String? = null
    ) {
        scope.launch {
            try {
                val participantCode = getParticipantCode()
                val logData = mutableMapOf<String, Any?>(
                    "participant_code" to participantCode,
                    "event_type" to eventType,
                    "status" to status,
                    "duration_ms" to durationMs,
                    "failure_reason" to failureReason,
                    "error_details" to errorDetails,
                    "timestamp" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                firestore.collection("system_validation_logs").add(logData)
            } catch (e: Exception) {
                Log.w("SystemValidationLogger", "Failed to log performance event: ${e.message}")
            }
        }
    }
}
