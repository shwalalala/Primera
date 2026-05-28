package com.example.primera.feature.insights.ui

import androidx.compose.ui.graphics.Color

sealed interface InsightsUiState {
    object Loading : InsightsUiState
    data class Success(val data: InsightsUiModel) : InsightsUiState
    data class Error(val message: String) : InsightsUiState
}

data class InsightsUiModel(
    val physiqueProgress: Float = 0f,
    val physiqueSubtitle: String = "",
    val physiqueStatus: String = "Healthy",
    val progressColor: Color = Color(0xFFA1D386),
    val activePeriod: String = "Weekly",
    val completedGoalsText: String = "",
    val goalsCompletionTip: String = "",
    val goalsCompletionTipColor: Color = Color(0xFFFFCC80),
    val healthWarningTip: String? = null,
    val healthWarningTipColor: Color = Color(0xFFCE93D8),
    val wellnessGoals: List<InsightGoalUiItem> = emptyList(),
    val weightTrend: ChartData = ChartData(),
    val activityTrend: ChartData = ChartData(),
    val moodTrend: ChartData = ChartData()
)

data class InsightGoalUiItem(
    val id: String,
    val icon: String,
    val title: String,
    val current: String,
    val target: String,
    val progress: Float,
    val color: Color
)

data class ChartData(
    val title: String = "",
    val dateRange: String = "",
    val values: List<Float> = emptyList(),
    val highlightedIndex: Int = -1
)
