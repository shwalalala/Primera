package com.example.primera.core.di

import android.content.Context
import com.example.primera.core.data.PreferenceRepository
import com.example.primera.core.data.PreferenceRepositoryImpl
import com.example.primera.feature.auth.data.AuthDataSource
import com.example.primera.feature.auth.data.AuthRepository
import com.example.primera.feature.auth.data.AuthRepositoryImpl
import com.example.primera.feature.checkins.data.CheckinsDataSource
import com.example.primera.feature.checkins.data.CheckinsRepository
import com.example.primera.feature.checkins.data.CheckinsRepositoryImpl
import com.example.primera.feature.dashboard.data.DashboardDataSource
import com.example.primera.feature.dashboard.data.DashboardRepository
import com.example.primera.feature.dashboard.data.DashboardRepositoryImpl
import com.example.primera.feature.onboarding.data.OnboardingRepository
import com.example.primera.feature.onboarding.data.OnboardingRepositoryImpl
import com.example.primera.feature.transcription.data.TranscriptionDataSource
import com.example.primera.feature.transcription.data.TranscriptionRepository
import com.example.primera.feature.transcription.data.TranscriptionRepositoryImpl
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
