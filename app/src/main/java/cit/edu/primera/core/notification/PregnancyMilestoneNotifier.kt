
package cit.edu.primera.core.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import cit.edu.primera.core.navigation.Routes
import cit.edu.primera.core.util.PregnancyCalculator
import cit.edu.primera.feature.notification.data.NotificationRepositoryImpl
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Handles pregnancy milestone notifications.
 *
 * Uses the signed-in user's pregnancy information
 * to calculate their current gestational week
 * and approximate pregnancy month.
 *
 * Each milestone is:
 * - Account-specific
 * - Recorded in notification history
 * - Displayed through Android notifications
 * - Protected against repeated delivery attempts
 *
 * App lifecycle integration is handled by MainActivity.
 */
class PregnancyMilestoneNotifier(
    private val context: Context
) {

    companion object {
        private const val TAG = "PregnancyMilestone"

        private const val PREFS_NAME =
            "primera_pregnancy_milestones"

        private const val NOTIFICATION_ID = 2100

        private const val MAX_GESTATIONAL_DAYS = 280
    }

    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val notificationRepository =
        NotificationRepositoryImpl(context.applicationContext)

    // ---------------------------------------------
    // PREGNANCY MONTH CALCULATION
    // ---------------------------------------------

    private fun monthForGestationalWeek(
        week: Int
    ): Int {
        return when (week) {
            in 0..4 -> 1
            in 5..8 -> 2
            in 9..13 -> 3
            in 14..17 -> 4
            in 18..22 -> 5
            in 23..27 -> 6
            in 28..31 -> 7
            in 32..35 -> 8
            in 36..40 -> 9
            else -> 0
        }
    }

    // ---------------------------------------------
    // EDUCATIONAL CONTENT
    // ---------------------------------------------

    private data class MilestoneContent(
        val title: String,
        val message: String
    )

    private fun milestoneContent(
        month: Int,
        week: Int
    ): MilestoneContent? {

        val prefix = "Around week $week, "

        return when (month) {

            1 -> MilestoneContent(
                title = "Your pregnancy journey begins! 💜",
                message = prefix +
                        "early pregnancy development is underway. " +
                        "The foundations for your baby's brain " +
                        "and other structures begin forming."
            )

            2 -> MilestoneContent(
                title = "Month 2: Early development!",
                message = prefix +
                        "your baby's early heart, brain, and " +
                        "other major structures are developing. " +
                        "Every week brings important changes."
            )

            3 -> MilestoneContent(
                title = "Month 3: Little features are forming!",
                message = prefix +
                        "your baby's face, hands, and feet " +
                        "continue becoming more defined."
            )

            4 -> MilestoneContent(
                title = "Month 4: New movements and reflexes!",
                message = prefix +
                        "your baby continues developing movement " +
                        "and reflexes, including swallowing motions."
            )

            5 -> MilestoneContent(
                title = "Month 5: Discovering sounds!",
                message = prefix +
                        "your baby's hearing continues developing, " +
                        "and you may begin noticing movement. " +
                        "Every pregnancy develops differently."
            )

            6 -> MilestoneContent(
                title = "Month 6: Growing every week!",
                message = prefix +
                        "your baby's movements and reflexes " +
                        "continue developing, while the eyes " +
                        "and other senses mature."
            )

            7 -> MilestoneContent(
                title = "Month 7: Continuing to grow!",
                message = prefix +
                        "your baby continues growing and developing " +
                        "as the third trimester progresses."
            )

            8 -> MilestoneContent(
                title = "Month 8: Preparing for the world!",
                message = prefix +
                        "your baby's eyes and other senses " +
                        "continue maturing in preparation for birth."
            )

            9 -> MilestoneContent(
                title = "Month 9: Getting closer!",
                message = prefix +
                        "your baby continues maturing as your " +
                        "estimated due date approaches. " +
                        "Every pregnancy follows its own timeline."
            )

            else -> null
        }
    }

    // ---------------------------------------------
    // CALENDAR DATE HELPER
    // ---------------------------------------------

    /**
     * Converts the local calendar date to an
     * integer day number.
     *
     * This avoids counting the hour of the day
     * when calculating pregnancy progress.
     */
    private fun epochDay(date: Date): Long {

        val local = Calendar.getInstance().apply {
            time = date
        }

        val utc = Calendar.getInstance(
            TimeZone.getTimeZone("UTC")
        ).apply {
            clear()

            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH)
            )
        }

        return Math.floorDiv(
            utc.timeInMillis,
            86_400_000L
        )
    }

    // ---------------------------------------------
    // NOTIFICATION PERMISSION
    // ---------------------------------------------

    private fun canPostNotifications(): Boolean {

        return Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    // ---------------------------------------------
    // CHECK CURRENT PREGNANCY MILESTONE
    // ---------------------------------------------

    suspend fun checkAndNotify() {

        val userId = auth.currentUser?.uid ?: return

        try {

            // Fetch pregnancy information only
            // for the current signed-in account.

            val userDocument = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDocument.exists()) {
                return
            }

            // Prevent account-switching mistakes.

            if (auth.currentUser?.uid != userId) {
                return
            }

            // -------------------------------------
            // READ EXISTING PREGNANCY DATES
            // -------------------------------------

            val explicitEdd = (
                    userDocument.getTimestamp("eddDate")
                        ?: userDocument.getTimestamp("dueDate")
                    )?.toDate()

            val scanDate = userDocument
                .getTimestamp("scanDate")
                ?.toDate()

            val scanWeeks = userDocument
                .getLong("scanWeeks")
                ?.toInt()

            val scanDays = userDocument
                .getLong("scanDays")
                ?.toInt()

            val lmpDate = userDocument
                .getTimestamp("lmpDate")
                ?.toDate()

            // A positive test date alone is not
            // considered sufficiently precise for
            // these automated month notifications.

            val hasDatingInformation =
                explicitEdd != null ||
                        (
                                scanDate != null &&
                                        (
                                                scanWeeks != null ||
                                                        scanDays != null
                                                )
                                ) ||
                        lmpDate != null

            if (!hasDatingInformation) {
                Log.d(
                    TAG,
                    "No valid pregnancy dating information"
                )
                return
            }

            // -------------------------------------
            // CALCULATE ESTIMATED DUE DATE
            // -------------------------------------

            val dueDate = PregnancyCalculator.calculateEdd(
                eddDate = explicitEdd,
                scanDate = scanDate,
                scanWeeks = scanWeeks,
                scanDays = scanDays,
                lmpDate = lmpDate
            ) ?: return

            // -------------------------------------
            // CALCULATE CURRENT PREGNANCY PROGRESS
            // -------------------------------------

            val currentDay = epochDay(Date())

            val dueDay = epochDay(dueDate)

            val gestationalDays =
                MAX_GESTATIONAL_DAYS -
                        (dueDay - currentDay).toInt()

            if (
                gestationalDays < 0 ||
                gestationalDays > MAX_GESTATIONAL_DAYS
            ) {
                Log.d(
                    TAG,
                    "Pregnancy date is outside milestone window"
                )
                return
            }

            val week = gestationalDays / 7

            val month = monthForGestationalWeek(week)

            if (month !in 1..9) {
                return
            }

            val content = milestoneContent(
                month = month,
                week = week
            ) ?: return

            // -------------------------------------
            // ACCOUNT-SPECIFIC MILESTONE KEY
            // -------------------------------------

            // Keep this exactly compatible with the
            // previous notifier's SharedPreferences
            // key, so already-shown Android alerts
            // will not be displayed again.

            val milestoneKey =
                "milestone_${userId}_${dueDay}_$month"

            val historyKey =
                "history_$milestoneKey"

            // Deterministic notification ID.
            // Firebase stores these under the
            // user's own notification collection.

            val historyNotificationId =
                "pregnancy_milestone_${dueDay}_$month"

            // -------------------------------------
            // SAVE TO PRIMERA NOTIFICATIONS PAGE
            // -------------------------------------

            // This is separate from Android delivery.
            // An already delivered milestone can be
            // added to in-app history without creating
            // another Android system notification.

            if (!preferences.getBoolean(
                    historyKey,
                    false
                )
            ) {

                // Verify account before saving.
                if (auth.currentUser?.uid != userId) {
                    return
                }

                val notification = NotificationItem(
                    id = historyNotificationId,
                    title = content.title,
                    message = content.message,
                    timestamp = System.currentTimeMillis(),
                    category =
                        NotificationItem.CATEGORY_PREGNANCY_MILESTONE,
                    destinationRoute = Routes.INSIGHT,
                    isRead = false
                )

                notificationRepository.addNotification(
                    userId = userId,
                    notification = notification
                )

                // The repository writes a local copy
                // and attempts the Firestore write.
                // It currently swallows remote errors,
                // so this means a storage attempt,
                // not guaranteed Firebase persistence.

                preferences.edit()
                    .putBoolean(historyKey, true)
                    .apply()

                Log.d(
                    TAG,
                    "Milestone history stored: " +
                            "month=$month, week=$week"
                )
            }

            // -------------------------------------
            // DISPLAY ANDROID NOTIFICATION
            // -------------------------------------

            // Don't repeat an already delivered
            // milestone from the earlier implementation.

            if (preferences.getBoolean(
                    milestoneKey,
                    false
                )
            ) {
                Log.d(
                    TAG,
                    "Android milestone already delivered"
                )
                return
            }

            if (!canPostNotifications()) {
                Log.d(
                    TAG,
                    "Android notification permission unavailable"
                )
                return
            }

            // Check again before showing any
            // pregnancy-related notification.

            if (auth.currentUser?.uid != userId) {
                return
            }

            NotificationHelper(context).showNotification(
                title = content.title,
                message = content.message,
                notificationId = NOTIFICATION_ID,
                destinationRoute = Routes.INSIGHT
            )

            // The existing helper does not report
            // whether Android displayed the alert.
            // This records a delivery attempt.

            preferences.edit()
                .putBoolean(milestoneKey, true)
                .apply()

            Log.d(
                TAG,
                "Android milestone dispatched: " +
                        "month=$month, week=$week"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Unable to evaluate pregnancy milestone",
                e
            )
        }
    }
}
