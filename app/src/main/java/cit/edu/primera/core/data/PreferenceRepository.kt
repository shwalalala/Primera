package cit.edu.primera.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface PreferenceRepository {
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
    fun clearLocalData()
}

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {
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
    }

    override suspend fun shouldShowOnboarding(userId: String): Boolean {
        if (userId.isBlank()) return true

        val isCompletedLocally = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED + userId, false)
        return !isCompletedLocally && try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!snapshot.exists()) return true

            val isCompletedInFirestore = (snapshot.getBoolean("onboardingCompleted") == true)
                    || (snapshot.get("birthday") != null)
                    || (snapshot.get("eddDate") != null)
                    || (snapshot.get("dueDate") != null)

            if (isCompletedInFirestore) {
                setOnboardingCompleted(userId)
                false
            } else {
                snapshot.getBoolean("onboardingCompleted") == false
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun setOnboardingCompleted(userId: String) {
        sharedPreferences.edit { 
            putBoolean(KEY_ONBOARDING_COMPLETED + userId, true)
            remove(KEY_ONBOARDING_STEP + userId)
        }
    }

    override fun getOnboardingLastStep(userId: String): String? {
        return sharedPreferences.getString(KEY_ONBOARDING_STEP + userId, null)
    }

    override fun setOnboardingLastStep(userId: String, step: String) {
        sharedPreferences.edit { putString(KEY_ONBOARDING_STEP + userId, step) }
    }

    override fun isWatchSyncEnabled(userId: String): Boolean {
        return sharedPreferences.getBoolean(KEY_WATCH_SYNC_ENABLED + userId, false)
    }

    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_WATCH_SYNC_ENABLED + userId, enabled) }
    }

    override fun getCustomOptions(userId: String, category: String): Set<String> {
        return sharedPreferences.getStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId, emptySet()) ?: emptySet()
    }

    override fun addCustomOption(userId: String, category: String, label: String) {
        val current = getCustomOptions(userId, category).toMutableSet()
        current.add(label)
        sharedPreferences.edit { putStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId, current) }
    }

    override fun hasEverLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_EVER_LOGGED_IN, false)
    }

    override fun setHasEverLoggedIn(hasLoggedIn: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HAS_EVER_LOGGED_IN, hasLoggedIn) }
    }

    override fun getSavedEmail(): String? {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, null)
    }

    override fun setSavedEmail(email: String?) {
        sharedPreferences.edit {
            if (email.isNullOrBlank()) {
                remove(KEY_SAVED_EMAIL)
            } else {
                putString(KEY_SAVED_EMAIL, email.trim().lowercase())
            }
        }
    }

    override fun getProfilePictureUri(userId: String): String? {
        if (userId.isBlank()) return null
        return sharedPreferences.getString(KEY_PROFILE_PICTURE_URI + userId, null)
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
        val key =
            if (userId.isBlank()) KEY_MIC_COACHMARK_SEEN + "guest" else KEY_MIC_COACHMARK_SEEN + userId
        return sharedPreferences.getBoolean(key, false)
    }

    override fun setMicCoachmarkDismissed(userId: String) {
        val key =
            if (userId.isBlank()) KEY_MIC_COACHMARK_SEEN + "guest" else KEY_MIC_COACHMARK_SEEN + userId
        sharedPreferences.edit { putBoolean(key, true) }
    }

    override fun clearLocalData() {
        val savedEmail = getSavedEmail()
        val hasLoggedIn = hasEverLoggedIn()
        sharedPreferences.edit {
            clear()
            if (!savedEmail.isNullOrBlank()) {
                putString(KEY_SAVED_EMAIL, savedEmail)
            }
            if (hasLoggedIn) {
                putBoolean(KEY_HAS_EVER_LOGGED_IN, true)
            }
        }
    }
}
