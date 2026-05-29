package com.example.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.dashboard.data.DashboardRepository
import com.example.primera.feature.dashboard.domain.DashboardBusinessLogic
import com.example.primera.feature.dashboard.domain.DashboardData
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
    private val goalsRepository: com.example.primera.feature.goals.data.GoalsRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            goalsRepository.ensureMandatoryGoals()
        }
    }

    val uiState: StateFlow<DashboardUiState> = repository.observeDashboardData()
        .map { data ->
            if (data == null) {
                DashboardUiState.Success(createDefaultUiModel())
            } else {
                DashboardUiState.Success(mapToUiModel(data))
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
            weekDays = getCurrentWeekDays()
        )
    }

    private fun mapToUiModel(data: DashboardData): DashboardUiModel {
        val week = DashboardBusinessLogic.getWeekNumber(data.dueDate)
        val isWatchSynced = data.steps > 0 || data.heartRateBpm > 0 || data.sleepHours > 0 || data.sleepMinutes > 0
        
        // BUG-001 Fix: For now, we don't have historical HR, so we just set trending to false 
        // unless we implement local caching or more complex DB reads.
        // Let's at least remove the hardcoded 'true' and '5'.
        
        return DashboardUiModel(
            userName = data.userName,
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = DashboardBusinessLogic.getTrimester(week),
            weekNumber = week,
            dayNumber = DashboardBusinessLogic.getDayNumber(data.dueDate),
            daysLeft = DashboardBusinessLogic.getDaysLeft(data.dueDate),
            babySize = DashboardBusinessLogic.getBabySize(week),
            babyEmoji = DashboardBusinessLogic.getBabyEmoji(week),
            heartRateBpm = data.heartRateBpm,
            heartRateTrendingUp = false, // Set to false by default until we have trend logic
            heartRateVsLastWeek = 0,    // Set to 0 until we have historical data
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
            weekDays = getCurrentWeekDays()
        )
    }

    private fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { time = date }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.add(Calendar.DAY_OF_YEAR, -1)
        val cal2 = Calendar.getInstance().apply { time = date }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Set to the first day of the week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        
        val days = mutableListOf<DashboardWeekDayItem>()
        val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")
        
        for (i in 0..6) {
            days.add(
                DashboardWeekDayItem(
                    initial = dayInitials[i],
                    date = calendar.get(Calendar.DAY_OF_MONTH),
                    isSelected = calendar.get(Calendar.DAY_OF_YEAR) == today
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun onViewAllLogs() {}
    fun onAddLog() {}
    fun onInputManually() {}

    // FEAT-002: Watch Sync Stub
    fun onSyncWatch() {
        viewModelScope.launch {
            // Simulate watch sync by generating random but plausible data
            val randomSteps = (5000..10000).random().toLong()
            val randomHR = (70..85).random().toLong()
            val randomSleepHours = (6..8).random().toLong()
            val randomSleepMinutes = (0..59).random().toLong()
            
            repository.updateHealthData(
                steps = randomSteps,
                heartRate = randomHR,
                sleepHours = randomSleepHours,
                sleepMinutes = randomSleepMinutes
            )
        }
    }

    // DB-002: Update Steps Goal
    fun onUpdateStepsGoal(goal: Long) {
        viewModelScope.launch {
            repository.updateStepsGoal(goal)
        }
    }
}
