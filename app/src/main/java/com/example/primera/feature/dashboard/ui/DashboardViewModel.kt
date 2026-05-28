package com.example.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.utils.DashboardBusinessLogic
import com.example.primera.feature.dashboard.data.repository.DashboardRepository
import com.example.primera.feature.dashboard.domain.model.DashboardData
import com.example.primera.core.theme.LogBaby
import com.example.primera.core.theme.LogNutrition
import com.example.primera.core.theme.LogOther
import com.example.primera.core.theme.LogPain
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeDashboardData()
        .map { data ->
            if (data == null) DashboardUiState.Error("User not found")
            else DashboardUiState.Success(mapToUiModel(data))
        }
        .catch { e ->
            emit(DashboardUiState.Error(e.message ?: "An unexpected error occurred"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    private fun mapToUiModel(data: DashboardData): DashboardUiModel {
        val week = DashboardBusinessLogic.getWeekNumber(data.dueDate)
        return DashboardUiModel(
            userName = data.userName,
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = DashboardBusinessLogic.getTrimester(week),
            weekNumber = week,
            dayNumber = DashboardBusinessLogic.getDayNumber(data.dueDate),
            daysLeft = DashboardBusinessLogic.getDaysLeft(data.dueDate),
            babySize = DashboardBusinessLogic.getBabySize(week),
            heartRateBpm = data.heartRateBpm,
            heartRateTrendingUp = true, // Placeholder logic
            heartRateVsLastWeek = 5,    // Placeholder logic
            steps = data.steps,
            stepsGoal = data.stepsGoal,
            sleepHours = data.sleepHours,
            sleepMinutes = data.sleepMinutes,
            sleepQuality = DashboardBusinessLogic.getSleepQuality(data.sleepHours, data.sleepMinutes),
            recentLogs = data.recentLogs.map { log ->
                DashboardLogUiItem(
                    category = log.category,
                    description = log.description,
                    time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(log.timestamp),
                    accentColor = getCategoryColor(log.category)
                )
            },
            weekDays = getDefaultWeekDays()
        )
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain" -> LogPain
            "nutrition", "food" -> LogNutrition
            "fetal movement", "baby" -> LogBaby
            else -> LogOther
        }
    }

    private fun getDefaultWeekDays() = listOf(
        DashboardWeekDayItem("S", 22, false),
        DashboardWeekDayItem("M", 23, true),
        DashboardWeekDayItem("T", 24, false),
        DashboardWeekDayItem("W", 25, false),
        DashboardWeekDayItem("T", 26, false),
        DashboardWeekDayItem("F", 27, false),
        DashboardWeekDayItem("S", 28, false)
    )

    fun onViewAllLogs() {}
    fun onAddLog() {}
    fun onInputManually() {}
}
