package com.example.primera.feature.onboarding.data

import com.example.primera.feature.onboarding.domain.OnboardingProfile

interface OnboardingRepository {
    suspend fun saveProfile(profile: OnboardingProfile): Result<Unit>
}
