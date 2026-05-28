package com.example.primera.core.data.repository

import android.content.Context
import android.content.SharedPreferences

interface PreferenceRepository {
    fun shouldShowOnboarding(): Boolean
    fun setOnboardingCompleted()
}

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("primera_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override fun shouldShowOnboarding(): Boolean {
        return !sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }
}
