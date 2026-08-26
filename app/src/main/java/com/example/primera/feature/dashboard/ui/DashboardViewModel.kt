package com.example.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.dashboard.data.DashboardRepository
import com.example.primera.feature.dashboard.domain.DashboardBusinessLogic
import com.example.primera.feature.dashboard.domain.DashboardData
import com.example.primera.feature.smartwatchconnection.data.HealthConnectManager
import com.example.primera.core.theme.LogBaby
import com.example.primera.core.theme.LogNutrition
import com.example.primera.core.theme.LogOther
import com.example.primera.core.theme.LogPain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val goalsRepository: com.example.primera.feature.goals.data.GoalsRepository,
    private val healthConnectManager: HealthConnectManager,
    private val preferenceRepository: com.example.primera.core.data.PreferenceRepository,
    private val networkMonitor: com.example.primera.core.util.NetworkMonitor,
) : ViewModel() {

    init {
        viewModelScope.launch {
            goalsRepository.ensureMandatoryGoals()
        }
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeDashboardData(),
        repository.observeHealthRecords()
    ) { data, healthRecords ->
        if (data == null) {
            DashboardUiState.Success(createDefaultUiModel())
        } else {
            DashboardUiState.Success(mapToUiModel(data, healthRecords))
        } as DashboardUiState
    }
        .onStart { 
            // Optional: emit Loading explicitly if needed
        }
        .catch { e ->
            // If the index is still building, we might get an error. 
            // We can choose to show an empty state or the error.
            if (e.message?.contains("index") == true) {
                emit(DashboardUiState.Success(createDefaultUiModel()))
            } else {
                emit(DashboardUiState.Error(e.message ?: "An unexpected error occurred"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    private fun createDefaultUiModel(): DashboardUiModel {
        return DashboardUiModel(
            userName = "Guest",
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = "Unknown",
            weekNumber = 1,
            dayNumber = 0,
            daysLeft = 0,
            babySize = "Unknown",
            babyEmoji = "👶",
            babyIllustration = null,
            heartRateBpm = 0,
            heartRateTrendingUp = false,
            heartRateVsLastWeek = 0,
            steps = 0,
            stepsGoal = 8000,
            sleepHours = 0,
            sleepMinutes = 0,
            sleepQuality = "Unknown",
            spO2 = null,
            isWatchSynced = false,
            recentLogs = emptyList(),
            weekDays = getCurrentWeekDays(),
            milestones = DashboardBusinessLogic.getWeeklyMilestones(1),
            symptoms = DashboardBusinessLogic.getWeeklySymptoms(1),
            articles = DashboardBusinessLogic.getWeeklyArticles(1)
        )
    }

    private fun mapToUiModel(data: DashboardData, healthRecords: List<com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth>): DashboardUiModel {
        val week = DashboardBusinessLogic.getWeekNumber(data.dueDate)
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        
        // Watch sync is active if the user has performed at least one sync session
        val isWatchSynced = preferenceRepository.isWatchSyncEnabled(userId)

        // Calculate heart rate trends
        val avgHrLastWeek = healthRecords
            .take(7)
            .mapNotNull { it.averageHeartRate }
            .average()

        val hrVsLastWeek = if (!avgHrLastWeek.isNaN() && (data.heartRateBpm > 0)) {
            (data.heartRateBpm - avgHrLastWeek).toInt()
        } else {
            0
        }
        
        return DashboardUiModel(
            userName = data.userName,
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = DashboardBusinessLogic.getTrimester(week),
            weekNumber = week,
            dayNumber = DashboardBusinessLogic.getDayNumber(data.dueDate),
            daysLeft = DashboardBusinessLogic.getDaysLeft(data.dueDate),
            babySize = DashboardBusinessLogic.getBabySize(week),
            babyEmoji = DashboardBusinessLogic.getBabyEmoji(week),
            babyIllustration = DashboardBusinessLogic.getBabyIllustration(week),
            heartRateBpm = data.heartRateBpm,
            heartRateTrendingUp = hrVsLastWeek > 0,
            heartRateVsLastWeek = hrVsLastWeek,
            steps = data.steps,
            stepsGoal = data.stepsGoal,
            sleepHours = data.sleepHours,
            sleepMinutes = data.sleepMinutes,
            sleepQuality = DashboardBusinessLogic.getSleepQuality(data.sleepHours, data.sleepMinutes),
            spO2 = data.spO2,
            isWatchSynced = isWatchSynced,
            recentLogs = data.recentLogs
                .map { log ->
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeText = if (isToday(log.timestamp)) {
                        "Today, ${sdf.format(log.timestamp)}"
                    } else if (isYesterday(log.timestamp)) {
                        "Yesterday, ${sdf.format(log.timestamp)}"
                    } else {
                        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(log.timestamp)
                    }

                    DashboardLogUiItem(
                        id = log.id,
                        category = log.category,
                        description = log.description,
                        time = timeText,
                        accentColor = getCategoryColor(log.category)
                    )
                },
            weekDays = getCurrentWeekDays(),
            milestones = DashboardBusinessLogic.getWeeklyMilestones(week),
            symptoms = DashboardBusinessLogic.getWeeklySymptoms(week),
            articles = DashboardBusinessLogic.getWeeklyArticles(week)
        )
    }

    private fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { time = date }
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    private fun isYesterday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.add(Calendar.DAY_OF_YEAR, -1)
        val cal2 = Calendar.getInstance().apply { time = date }
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain" -> LogPain
            "nutrition", "food" -> LogNutrition
            "fetal movement", "baby" -> LogBaby
            else -> LogOther
        }
    }

    private fun getCurrentWeekDays(): List<DashboardWeekDayItem> {
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]
        
        // Set to the first day of the week (Sunday)
        calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
        
        val days = mutableListOf<DashboardWeekDayItem>()
        val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")
        
        for (i in 0..6) {
            days.add(
                DashboardWeekDayItem(
                    initial = dayInitials[i],
                    date = calendar[Calendar.DAY_OF_MONTH],
                    isSelected = calendar[Calendar.DAY_OF_YEAR] == today
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun onViewAllLogs() {}
    fun onAddLog() {}
    fun onInputManually() {}

    // FEAT-002: Watch Sync with Actual Data
    fun onSyncWatch() {
        viewModelScope.launch {
            try {
                if (healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()) {
                    // Start sync process
                    val healthData = healthConnectManager.readTodaySmartwatchHealth()
                    
                    // Save to repository (Firestore)
                    repository.updateHealthData(
                        steps = healthData.steps,
                        heartRate = healthData.currentHeartRate ?: 0L,
                        sleepHours = healthData.sleepMinutes / 60,
                        sleepMinutes = healthData.sleepMinutes % 60,
                        spO2 = healthData.spO2?.toLong()
                    )
                    
                    // Also save to historical records for consistency across all screens
                    repository.saveHistoricalRecord(healthData)
                    
                    // Mark as synced locally for this user session
                    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    preferenceRepository.setWatchSyncEnabled(userId, enabled = true)
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Sync failed during dashboard 'Sync Now' click", e)
            }
        }
    }

    // DB-002: Update Steps Goal
    fun onUpdateStepsGoal(goal: Long) {
        viewModelScope.launch {
            repository.updateStepsGoal(goal)
        }
    }
}
