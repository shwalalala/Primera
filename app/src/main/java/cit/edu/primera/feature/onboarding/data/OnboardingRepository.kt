package cit.edu.primera.feature.onboarding.data

import cit.edu.primera.feature.onboarding.domain.OnboardingProfile

interface OnboardingRepository {
    suspend fun saveProfile(profile: OnboardingProfile): Result<Unit>
}
