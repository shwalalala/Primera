package com.example.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import com.example.primera.feature.health.domain.model.HealthStatsModel

data class DashboardUiState(
    val userName: String = "",
    val timeOfDay: String = "",
    val trimester: String = "",
    val weekNumber: Int = 0,
    val dayNumber: Int = 0,
    val daysLeft: Int = 0,
    val babySize: String = "",
    val healthStats: HealthStatsModel? = null,
    val sleepQuality: String = "",
    val recentLogs: List<DashboardLogUiModel> = emptyList(),
    val wellnessGoals: List<DashboardWellnessGoalUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class DashboardLogUiModel(
    val category: String,
    val message: String,
    val time: String,
    val color: Color
)

data class DashboardWellnessGoalUiModel(
    val label: String,
    val emoji: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val color: Color
)
