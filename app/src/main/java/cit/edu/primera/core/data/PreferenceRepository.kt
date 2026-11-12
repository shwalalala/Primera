
package cit.edu.primera.core.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface PreferenceRepository {
    fun getDeviceId(): String
    suspend fun shouldShowOnboarding(userId: String): Boolean
    fun setOnboardingCompleted(userId: String)
    fun getOnboardingLastStep(userId: String): String?
    fun setOnboardingLastStep(userId: String, step: String)
    fun isWatchSyncEnabled(userId: String): Boolean
    fun setWatchSyncEnabled(userId: String, enabled: Boolean)
    fun getCustomOptions(userId: String, category: String): Set<String>
    fun addCustomOption(userId: String, category: String, label: String)
    fun hasEverLoggedIn(): Boolean
    fun setHasEverLoggedIn(hasLoggedIn: Boolean)
    fun getSavedEmail(): String?
    fun setSavedEmail(email: String?)
    fun getProfilePictureUri(userId: String): String?
    fun setProfilePictureUri(userId: String, uri: String?)
    fun hasSeenMicCoachmark(userId: String): Boolean
    fun setMicCoachmarkDismissed(userId: String)
    fun isReminderEnabled(category: String): Boolean
    fun setReminderEnabled(category: String, enabled: Boolean)
    fun getReminderTime(category: String): Pair<Int, Int>
    fun setReminderTime(category: String, hour: Int, minute: Int)
    fun clearLocalData()
}

class PreferenceRepositoryImpl(private val context: Context) : PreferenceRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("primera_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed_"
        private const val KEY_ONBOARDING_STEP = "onboarding_step_"
        private const val KEY_WATCH_SYNC_ENABLED = "watch_sync_enabled_"
        private const val KEY_CUSTOM_OPTIONS_PREFIX = "custom_options_"
        private const val KEY_HAS_EVER_LOGGED_IN = "has_ever_logged_in"
        private const val KEY_SAVED_EMAIL = "saved_remember_me_email"
        private const val KEY_PROFILE_PICTURE_URI = "profile_picture_uri_"
        private const val KEY_MIC_COACHMARK_SEEN = "mic_coachmark_seen_"
        private const val KEY_PERSISTENT_DEVICE_ID = "persistent_local_device_id"
        private const val KEY_LAST_COMPLETED_USER_ID = "last_completed_user_id_on_device"
        private const val KEY_LAST_TOOLTIP_USER_ID = "last_tooltip_user_id_on_device"
    }

    override fun getDeviceId(): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                getOrCreatePersistentDeviceId()
            }
        } catch (_: Exception) {
            getOrCreatePersistentDeviceId()
        }
    }

    private fun getOrCreatePersistentDeviceId(): String {
        var localId = sharedPreferences.getString(KEY_PERSISTENT_DEVICE_ID, null)

        if (localId.isNullOrBlank()) {
            localId = UUID.randomUUID().toString()
            sharedPreferences.edit {
                putString(KEY_PERSISTENT_DEVICE_ID, localId)
            }
        }

        return localId
    }

    override suspend fun shouldShowOnboarding(userId: String): Boolean {
        if (userId.isBlank()) return true

        val deviceId = getDeviceId()
        val localKey = "${KEY_ONBOARDING_COMPLETED}${userId}_${deviceId}"

        // Preserve existing per-device and legacy local checks.
        val isCompletedLocally =
            sharedPreferences.getBoolean(localKey, false) ||
                    sharedPreferences.getBoolean(
                        KEY_ONBOARDING_COMPLETED + userId,
                        false
                    )

        val lastCompletedUserOnMachine =
            sharedPreferences.getString(
                KEY_LAST_COMPLETED_USER_ID,
                null
            )

        val localMatchesActiveUser =
            isCompletedLocally &&
                    lastCompletedUserOnMachine == userId

        // Firebase is the primary source of truth when available.
        val isServerCompletedForUserAndDevice = try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                false
            } else {
                val completedDeviceIds =
                    (snapshot.get("completedDeviceIds") as? List<*>)
                        ?.mapNotNull { it?.toString() }
                        ?: emptyList()

                val completedDevicesMap =
                    snapshot.get("completedDevices") as? Map<*, *>

                val deviceCompletedInMap =
                    completedDevicesMap?.get(deviceId) == true

                // Preserve the existing legacy completion checks
                // for accounts created before device tracking.
                val legacyCompleted =
                    snapshot.getBoolean("onboardingCompleted") == true ||
                            snapshot.get("birthday") != null ||
                            snapshot.get("eddDate") != null ||
                            snapshot.get("dueDate") != null

                val serverMatchesPairing =
                    completedDeviceIds.contains(deviceId) ||
                            deviceCompletedInMap ||
                            legacyCompleted

                // Preserve the teammate implementation that
                // registers a verified completed device.
                if (
                    serverMatchesPairing &&
                    !completedDeviceIds.contains(deviceId)
                ) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .update(
                                "completedDeviceIds",
                                FieldValue.arrayUnion(deviceId)
                            )
                    } catch (_: Exception) {
                        // Keep onboarding navigation functional
                        // if device registration fails.
                    }
                }

                serverMatchesPairing
            }
        } catch (_: Exception) {
            // If Firestore cannot be reached, retain the
            // existing offline fallback behavior.
            return !localMatchesActiveUser
        }

        // FIX:
        // A completed account verified by Firebase must not
        // repeat onboarding solely because local preferences
        // were cleared, changed, or belong to another user.
        if (isServerCompletedForUserAndDevice) {

            // Restore only local completion information.
            // Do not overwrite the user's profile in Firestore.
            sharedPreferences.edit {
                putBoolean(localKey, true)
                putBoolean(
                    KEY_ONBOARDING_COMPLETED + userId,
                    true
                )
                putString(
                    KEY_LAST_COMPLETED_USER_ID,
                    userId
                )
                remove(KEY_ONBOARDING_STEP + userId)
            }

            return false
        }

        // Firestore responded successfully but did not
        // confirm completed onboarding for this account.
        return true
    }

    override fun setOnboardingCompleted(userId: String) {
        if (userId.isBlank()) return

        val deviceId = getDeviceId()
        val localKey = "${KEY_ONBOARDING_COMPLETED}${userId}_${deviceId}"

        sharedPreferences.edit {
            putBoolean(localKey, true)
            putBoolean(KEY_ONBOARDING_COMPLETED + userId, true)
            putString(KEY_LAST_COMPLETED_USER_ID, userId)
            remove(KEY_ONBOARDING_STEP + userId)
        }

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "onboardingCompleted" to true,
                        "completedDeviceIds" to FieldValue.arrayUnion(deviceId)
                    )
                )
        } catch (_: Exception) {
        }
    }

    override fun getOnboardingLastStep(userId: String): String? {
        return sharedPreferences.getString(KEY_ONBOARDING_STEP + userId, null)
    }

    override fun setOnboardingLastStep(userId: String, step: String) {
        sharedPreferences.edit {
            putString(KEY_ONBOARDING_STEP + userId, step)
        }
    }

    override fun isWatchSyncEnabled(userId: String): Boolean {
        return sharedPreferences.getBoolean(
            KEY_WATCH_SYNC_ENABLED + userId,
            false
        )
    }

    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_WATCH_SYNC_ENABLED + userId, enabled)
        }
    }

    override fun getCustomOptions(
        userId: String,
        category: String
    ): Set<String> {
        return sharedPreferences.getStringSet(
            KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId,
            emptySet()
        ) ?: emptySet()
    }

    override fun addCustomOption(
        userId: String,
        category: String,
        label: String
    ) {
        val current = getCustomOptions(userId, category).toMutableSet()
        current.add(label)

        sharedPreferences.edit {
            putStringSet(
                KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId,
                current
            )
        }
    }

    override fun hasEverLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(
            KEY_HAS_EVER_LOGGED_IN,
            false
        )
    }

    override fun setHasEverLoggedIn(hasLoggedIn: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_HAS_EVER_LOGGED_IN, hasLoggedIn)
        }
    }

    override fun getSavedEmail(): String? {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, null)
    }

    override fun setSavedEmail(email: String?) {
        sharedPreferences.edit {
            if (email.isNullOrBlank()) {
                remove(KEY_SAVED_EMAIL)
            } else {
                putString(
                    KEY_SAVED_EMAIL,
                    email.trim().lowercase()
                )
            }
        }
    }

    override fun getProfilePictureUri(userId: String): String? {
        if (userId.isBlank()) return null

        return sharedPreferences.getString(
            KEY_PROFILE_PICTURE_URI + userId,
            null
        )
    }

    override fun setProfilePictureUri(userId: String, uri: String?) {
        if (userId.isBlank()) return

        sharedPreferences.edit {
            if (uri.isNullOrBlank()) {
                remove(KEY_PROFILE_PICTURE_URI + userId)
            } else {
                putString(KEY_PROFILE_PICTURE_URI + userId, uri)
            }
        }
    }

    override fun hasSeenMicCoachmark(userId: String): Boolean {
        val effectiveUser = if (userId.isBlank()) "guest" else userId
        val deviceId = getDeviceId()
        val localKey = "${KEY_MIC_COACHMARK_SEEN}${effectiveUser}_${deviceId}"

        val isLocallySeen =
            sharedPreferences.getBoolean(localKey, false) ||
                    sharedPreferences.getBoolean(
                        KEY_MIC_COACHMARK_SEEN + effectiveUser,
                        false
                    )

        val lastTooltipUser =
            sharedPreferences.getString(
                KEY_LAST_TOOLTIP_USER_ID,
                null
            )

        val localMatchesCurrentUser =
            isLocallySeen && lastTooltipUser == effectiveUser

        return localMatchesCurrentUser
    }

    override fun setMicCoachmarkDismissed(userId: String) {
        val effectiveUser = if (userId.isBlank()) "guest" else userId
        val deviceId = getDeviceId()
        val localKey = "${KEY_MIC_COACHMARK_SEEN}${effectiveUser}_${deviceId}"

        sharedPreferences.edit {
            putBoolean(localKey, true)
            putBoolean(KEY_MIC_COACHMARK_SEEN + effectiveUser, true)
            putString(KEY_LAST_TOOLTIP_USER_ID, effectiveUser)
        }

        if (effectiveUser != "guest") {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(effectiveUser)
                    .update(
                        "seenMicCoachmarkDevices",
                        FieldValue.arrayUnion(deviceId)
                    )
            } catch (_: Exception) {
            }
        }
    }

    override fun isReminderEnabled(category: String): Boolean {
        return sharedPreferences.getBoolean(
            "reminder_enabled_${category.lowercase()}",
            true
        )
    }

    override fun setReminderEnabled(category: String, enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(
                "reminder_enabled_${category.lowercase()}",
                enabled
            )
        }
    }

    override fun getReminderTime(category: String): Pair<Int, Int> {
        val defaultHour = when (category.lowercase()) {
            "checkin" -> 9
            "hydration" -> 14
            "wellness" -> 20
            else -> 9
        }

        val defaultMinute = 0

        val hour = sharedPreferences.getInt(
            "reminder_hour_${category.lowercase()}",
            defaultHour
        )

        val minute = sharedPreferences.getInt(
            "reminder_minute_${category.lowercase()}",
            defaultMinute
        )

        return Pair(hour, minute)
    }

    override fun setReminderTime(
        category: String,
        hour: Int,
        minute: Int
    ) {
        sharedPreferences.edit {
            putInt("reminder_hour_${category.lowercase()}", hour)
            putInt("reminder_minute_${category.lowercase()}", minute)
        }
    }

    override fun clearLocalData() {
        val savedEmail = getSavedEmail()
        val hasLoggedIn = hasEverLoggedIn()
        val deviceId = getDeviceId()

        sharedPreferences.edit {
            clear()

            if (!savedEmail.isNullOrBlank()) {
                putString(KEY_SAVED_EMAIL, savedEmail)
            }

            if (hasLoggedIn) {
                putBoolean(KEY_HAS_EVER_LOGGED_IN, true)
            }

            putString(KEY_PERSISTENT_DEVICE_ID, deviceId)
        }
    }
}
