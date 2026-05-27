package com.example.primera.feature.onboarding.data.repository

import com.example.primera.feature.onboarding.domain.model.OnboardingProfile

interface OnboardingRepository {
    suspend fun saveProfile(profile: OnboardingProfile): Result<Unit>
}
