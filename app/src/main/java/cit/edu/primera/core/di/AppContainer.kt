package cit.edu.primera.core.di

import android.content.Context
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.core.data.PreferenceRepositoryImpl
import cit.edu.primera.feature.auth.data.AuthDataSource
import cit.edu.primera.feature.auth.data.AuthRepository
import cit.edu.primera.feature.auth.data.AuthRepositoryImpl
import cit.edu.primera.feature.checkins.data.CheckinsDataSource
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.checkins.data.CheckinsRepositoryImpl
import cit.edu.primera.feature.dashboard.data.DashboardDataSource
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.dashboard.data.DashboardRepositoryImpl
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.data.OnboardingRepositoryImpl
import cit.edu.primera.feature.transcription.data.TranscriptionDataSource
import cit.edu.primera.feature.transcription.data.TranscriptionRepository
import cit.edu.primera.feature.transcription.data.TranscriptionRepositoryImpl
import cit.edu.primera.feature.goals.data.GoalsRepository
import cit.edu.primera.feature.goals.data.GoalsRepositoryImpl
import cit.edu.primera.feature.smartwatchconnection.data.HealthConnectManager
import cit.edu.primera.core.util.NetworkMonitor
import cit.edu.primera.core.util.ConnectivityManagerNetworkMonitor
import cit.edu.primera.feature.transcription.domain.SymptomExtractor
import cit.edu.primera.feature.transcription.domain.KeywordSymptomExtractor
import cit.edu.primera.feature.transcription.domain.GeminiSymptomExtractor
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val authRepository: AuthRepository
    val dashboardRepository: DashboardRepository
    val onboardingRepository: OnboardingRepository
    val transcriptionRepository: TranscriptionRepository
    val checkinsRepository: CheckinsRepository
    val goalsRepository: GoalsRepository
    val preferenceRepository: PreferenceRepository
    val healthConnectManager: HealthConnectManager
    val networkMonitor: NetworkMonitor
    val symptomExtractor: SymptomExtractor
}

class AppContainerImpl(private val context: Context) : AppContainer {
    
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context)
    }

    override val symptomExtractor: SymptomExtractor by lazy {
        val geminiApiKey = cit.edu.primera.BuildConfig.GEMINI_API_KEY
        if (geminiApiKey.isNotBlank()) {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiApiKey,
            )
            GeminiSymptomExtractor(model)
        } else {
            KeywordSymptomExtractor()
        }
    }

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

    override val goalsRepository: GoalsRepository by lazy {
        GoalsRepositoryImpl()
    }

    override val preferenceRepository: PreferenceRepository by lazy {
        PreferenceRepositoryImpl(context)
    }

    override val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(context)
    }
}
