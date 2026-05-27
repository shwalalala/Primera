package com.example.primera.feature.onboarding.data.repository

import com.example.primera.feature.onboarding.data.datasource.OnboardingDataSource
import com.example.primera.feature.onboarding.domain.model.OnboardingProfile

class OnboardingRepositoryImpl(
    private val dataSource: OnboardingDataSource
) : OnboardingRepository {

    override suspend fun saveProfile(profile: OnboardingProfile): Result<Unit> {
        return dataSource.saveProfile(profile)
    }
}
