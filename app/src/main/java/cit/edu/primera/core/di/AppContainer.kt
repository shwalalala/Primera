
package cit.edu.primera.core.di

import android.content.Context
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.core.data.PreferenceRepositoryImpl
import cit.edu.primera.core.util.ConnectivityManagerNetworkMonitor
import cit.edu.primera.core.util.HealthSummaryDataLoader
import cit.edu.primera.core.util.NetworkMonitor
import cit.edu.primera.feature.auth.data.AuthDataSource
import cit.edu.primera.feature.auth.data.AuthRepository
import cit.edu.primera.feature.auth.data.AuthRepositoryImpl
import cit.edu.primera.feature.checkins.data.CheckinsDataSource
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.checkins.data.CheckinsRepositoryImpl
import cit.edu.primera.feature.checkins.data.PrenatalDataSource
import cit.edu.primera.feature.dashboard.data.DashboardDataSource
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.dashboard.data.DashboardRepositoryImpl
import cit.edu.primera.feature.goals.data.GoalsRepository
import cit.edu.primera.feature.goals.data.GoalsRepositoryImpl
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.data.OnboardingRepositoryImpl
import cit.edu.primera.feature.smartwatchconnection.data.HealthConnectManager
import cit.edu.primera.feature.transcription.data.TranscriptionDataSource
import cit.edu.primera.feature.transcription.data.TranscriptionRepository
import cit.edu.primera.feature.transcription.data.TranscriptionRepositoryImpl
import cit.edu.primera.feature.transcription.domain.GeminiSymptomExtractor
import cit.edu.primera.feature.transcription.domain.GeminiVoiceTextCorrector
import cit.edu.primera.feature.transcription.domain.KeywordSymptomExtractor
import cit.edu.primera.feature.transcription.domain.SymptomExtractor
import cit.edu.primera.feature.transcription.domain.VoiceTextCorrector
import cit.edu.primera.feature.vaccination.data.VaccinationRepository
import cit.edu.primera.feature.newseducation.data.NewsEducationRepository
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

    val notificationRepository:
            cit.edu.primera.feature.notification.data.NotificationRepository

    val reminderManager:
            cit.edu.primera.core.notification.ReminderManager

    val healthConnectManager: HealthConnectManager
    val networkMonitor: NetworkMonitor
    val symptomExtractor: SymptomExtractor
    val voiceTextCorrector: VoiceTextCorrector

    val geminiContentRepository:
            cit.edu.primera.feature.dashboard.data.GeminiContentRepository

    // Health Summary PDF data collection
    val healthSummaryDataLoader: HealthSummaryDataLoader

    // Local changes: Vaccination and News Education
    val vaccinationRepository: VaccinationRepository
    val newsEducationRepository: NewsEducationRepository
}

class AppContainerImpl(
    private val context: Context
) : AppContainer {

    // Network monitoring
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context)
    }

    // Gemini content repository
    override val geminiContentRepository:
            cit.edu.primera.feature.dashboard.data.GeminiContentRepository by lazy {

        val geminiApiKey = cit.edu.primera.BuildConfig.GEMINI_API_KEY

        if (geminiApiKey.isNotBlank()) {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiApiKey
            )

            cit.edu.primera.feature.dashboard.data.GeminiContentRepository(
                model
            )
        } else {
            cit.edu.primera.feature.dashboard.data.GeminiContentRepository(
                null
            )
        }
    }

    // Symptom extraction
    override val symptomExtractor: SymptomExtractor by lazy {

        val geminiApiKey = cit.edu.primera.BuildConfig.GEMINI_API_KEY

        if (geminiApiKey.isNotBlank()) {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiApiKey
            )

            GeminiSymptomExtractor(model)
        } else {
            KeywordSymptomExtractor()
        }
    }

    // Voice text correction
    override val voiceTextCorrector: VoiceTextCorrector by lazy {

        val geminiApiKey = cit.edu.primera.BuildConfig.GEMINI_API_KEY

        if (geminiApiKey.isNotBlank()) {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiApiKey
            )

            GeminiVoiceTextCorrector(model)
        } else {
            GeminiVoiceTextCorrector(null)
        }
    }

    // ---------------------------------------------
    // AUTH DEPENDENCIES
    // ---------------------------------------------

    private val authDataSource: AuthDataSource by lazy {
        AuthDataSource()
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authDataSource)
    }

    // ---------------------------------------------
    // DASHBOARD DEPENDENCIES
    // ---------------------------------------------

    private val dashboardDataSource: DashboardDataSource by lazy {
        DashboardDataSource(preferenceRepository)
    }

    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(dashboardDataSource)
    }

    override val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl()
    }

    // ---------------------------------------------
    // CHECK-IN DEPENDENCIES
    // ---------------------------------------------

    private val checkinsDataSource: CheckinsDataSource by lazy {
        CheckinsDataSource()
    }

    override val checkinsRepository: CheckinsRepository by lazy {
        CheckinsRepositoryImpl(
            checkinsDataSource,
            preferenceRepository
        )
    }

    // ---------------------------------------------
    // PRENATAL DEPENDENCIES
    // ---------------------------------------------

    private val prenatalDataSource: PrenatalDataSource by lazy {
        PrenatalDataSource()
    }

    // ---------------------------------------------
    // HEALTH SUMMARY PDF DEPENDENCIES
    // ---------------------------------------------

    override val healthSummaryDataLoader: HealthSummaryDataLoader by lazy {
        HealthSummaryDataLoader(
            dashboardRepository = dashboardRepository,
            checkinsRepository = checkinsRepository,
            prenatalDataSource = prenatalDataSource
        )
    }

    // ---------------------------------------------
    // TRANSCRIPTION DEPENDENCIES
    // ---------------------------------------------

    private val transcriptionDataSource: TranscriptionDataSource by lazy {
        TranscriptionDataSource(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
    }

    override val transcriptionRepository: TranscriptionRepository by lazy {
        TranscriptionRepositoryImpl(transcriptionDataSource)
    }

    // ---------------------------------------------
    // GOALS DEPENDENCIES
    // ---------------------------------------------

    override val goalsRepository: GoalsRepository by lazy {
        GoalsRepositoryImpl()
    }

    // ---------------------------------------------
    // PREFERENCES DEPENDENCIES
    // ---------------------------------------------

    override val preferenceRepository: PreferenceRepository by lazy {
        PreferenceRepositoryImpl(context)
    }

    // ---------------------------------------------
    // NOTIFICATION DEPENDENCIES
    // ---------------------------------------------

    override val notificationRepository:
            cit.edu.primera.feature.notification.data.NotificationRepository by lazy {

        cit.edu.primera.feature.notification.data.NotificationRepositoryImpl(
            context
        )
    }

    override val reminderManager:
            cit.edu.primera.core.notification.ReminderManager by lazy {

        cit.edu.primera.core.notification.ReminderManager(context)
    }

    // ---------------------------------------------
    // HEALTH CONNECT DEPENDENCIES
    // ---------------------------------------------

    override val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(context)
    }

    // ---------------------------------------------
    // VACCINATION DEPENDENCIES
    // ---------------------------------------------

    override val vaccinationRepository: VaccinationRepository by lazy {
        cit.edu.primera.feature.vaccination.data.VaccinationRepositoryImpl()
    }

    // ---------------------------------------------
    // NEWS EDUCATION DEPENDENCIES
    // ---------------------------------------------

    override val newsEducationRepository: NewsEducationRepository by lazy {
        cit.edu.primera.feature.newseducation.data.NewsEducationRepositoryImpl()
    }
}