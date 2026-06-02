package com.example.primera.core.data

import android.content.Context
import android.content.SharedPreferences

interface PreferenceRepository {
    fun shouldShowOnboarding(userId: String): Boolean
    fun setOnboardingCompleted(userId: String)
    fun isWatchSyncEnabled(userId: String): Boolean
    fun setWatchSyncEnabled(userId: String, enabled: Boolean)
    fun getCustomOptions(userId: String, category: String): Set<String>
    fun addCustomOption(userId: String, category: String, label: String)
}

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("primera_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed_"
        private const val KEY_WATCH_SYNC_ENABLED = "watch_sync_enabled_"
        private const val KEY_CUSTOM_OPTIONS_PREFIX = "custom_options_"
    }

    override fun shouldShowOnboarding(userId: String): Boolean {
        return !sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED + userId, false)
    }

    override fun setOnboardingCompleted(userId: String) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED + userId, true).apply()
    }

    override fun isWatchSyncEnabled(userId: String): Boolean {
        return sharedPreferences.getBoolean(KEY_WATCH_SYNC_ENABLED + userId, false)
    }

    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_WATCH_SYNC_ENABLED + userId, enabled).apply()
    }

    override fun getCustomOptions(userId: String, category: String): Set<String> {
        return sharedPreferences.getStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId, emptySet()) ?: emptySet()
    }

    override fun addCustomOption(userId: String, category: String, label: String) {
        val current = getCustomOptions(userId, category).toMutableSet()
        current.add(label)
        sharedPreferences.edit().putStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase() + "_" + userId, current).apply()
    }
}
