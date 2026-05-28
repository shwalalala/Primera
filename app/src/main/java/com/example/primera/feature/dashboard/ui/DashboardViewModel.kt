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
        val isWatchSynced = data.steps > 0 || data.heartRateBpm > 0 || data.sleepHours > 0 || data.sleepMinutes > 0
        
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
            heartRateTrendingUp = true, 
            heartRateVsLastWeek = 5,    
            steps = data.steps,
            stepsGoal = data.stepsGoal,
            sleepHours = data.sleepHours,
            sleepMinutes = data.sleepMinutes,
            sleepQuality = DashboardBusinessLogic.getSleepQuality(data.sleepHours, data.sleepMinutes),
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
}
