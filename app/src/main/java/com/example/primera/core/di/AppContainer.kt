package com.example.primera.core.di

import android.content.Context
import com.example.primera.core.data.repository.PreferenceRepository
import com.example.primera.core.data.repository.PreferenceRepositoryImpl
import com.example.primera.feature.auth.data.datasource.AuthDataSource
import com.example.primera.feature.auth.data.repository.AuthRepository
import com.example.primera.feature.auth.data.repository.AuthRepositoryImpl
import com.example.primera.feature.checkins.data.datasource.CheckinsDataSource
import com.example.primera.feature.checkins.data.repository.CheckinsRepository
import com.example.primera.feature.checkins.data.repository.CheckinsRepositoryImpl
import com.example.primera.feature.dashboard.data.datasource.DashboardDataSource
import com.example.primera.feature.dashboard.data.repository.DashboardRepository
import com.example.primera.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.example.primera.feature.onboarding.data.repository.OnboardingRepository
import com.example.primera.feature.onboarding.data.repository.OnboardingRepositoryImpl
import com.example.primera.feature.transcription.data.datasource.TranscriptionDataSource
import com.example.primera.feature.transcription.data.repository.TranscriptionRepository
import com.example.primera.feature.transcription.data.repository.TranscriptionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val authRepository: AuthRepository
    val dashboardRepository: DashboardRepository
    val onboardingRepository: OnboardingRepository
    val transcriptionRepository: TranscriptionRepository
    val checkinsRepository: CheckinsRepository
    val preferenceRepository: PreferenceRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {
    
    // Auth dependencies
    private val authDataSource: AuthDataSource by lazy {
        AuthDataSource()
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authDataSource)
    }

    // Dashboard dependencies
    private val dashboardDataSource: DashboardDataSource by lazy {
        DashboardDataSource()
    }

    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(dashboardDataSource)
    }

    override val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl()
    }

    // Check-ins dependencies
    private val checkinsDataSource: CheckinsDataSource by lazy {
        CheckinsDataSource()
    }

    override val checkinsRepository: CheckinsRepository by lazy {
        CheckinsRepositoryImpl(checkinsDataSource, preferenceRepository)
    }

    // Transcription dependencies
    private val transcriptionDataSource: TranscriptionDataSource by lazy {
        TranscriptionDataSource(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    }

    override val transcriptionRepository: TranscriptionRepository by lazy {
        TranscriptionRepositoryImpl(transcriptionDataSource)
    }

    override val preferenceRepository: PreferenceRepository by lazy {
        PreferenceRepositoryImpl(context)
    }
}
