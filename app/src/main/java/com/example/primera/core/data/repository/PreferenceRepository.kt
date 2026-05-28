package com.example.primera.core.data.repository

import android.content.Context
import android.content.SharedPreferences

interface PreferenceRepository {
    fun shouldShowOnboarding(): Boolean
    fun setOnboardingCompleted()
    fun getCustomOptions(category: String): Set<String>
    fun addCustomOption(category: String, label: String)
}

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("primera_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_CUSTOM_OPTIONS_PREFIX = "custom_options_"
    }

    override fun shouldShowOnboarding(): Boolean {
        return !sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    override fun getCustomOptions(category: String): Set<String> {
        return sharedPreferences.getStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase(), emptySet()) ?: emptySet()
    }

    override fun addCustomOption(category: String, label: String) {
        val current = getCustomOptions(category).toMutableSet()
        current.add(label)
        sharedPreferences.edit().putStringSet(KEY_CUSTOM_OPTIONS_PREFIX + category.lowercase(), current).apply()
    }
}
