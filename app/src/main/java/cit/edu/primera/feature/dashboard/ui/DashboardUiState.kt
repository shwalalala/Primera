package cit.edu.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import cit.edu.primera.feature.dashboard.domain.ArticleInfo

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardUiModel) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class DashboardUiModel(
    val userName: String,
    val timeOfDay: String,
    val trimester: String,
    val weekNumber: Int,
    val dayNumber: Int,
    val daysLeft: Int,
    val babySize: String,
    val babyEmoji: String,
    val babyIllustration: Int? = null,
    val heartRateBpm: Int,
    val heartRateTrendingUp: Boolean,
    val heartRateVsLastWeek: Int,
    val steps: Int,
    val stepsGoal: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val sleepQuality: String,
    val spO2: Int?,
    val isWatchSynced: Boolean,
    val isGestationalAgeTentative: Boolean = false,
    val inaccuracyWarning: String? = null,
    val recentLogs: List<DashboardLogUiItem>,
    val weekDays: List<DashboardWeekDayItem>,
    val milestones: List<String> = emptyList(),
    val symptoms: List<String> = emptyList(),
    val articles: List<ArticleInfo> = emptyList()
)

data class DashboardLogUiItem(
    val id: String? = null,
    val category: String,
    val description: String,
    val time: String,
    val accentColor: Color
)

data class DashboardWeekDayItem(
    val initial: String,
    val date: Int,
    val isSelected: Boolean
)
