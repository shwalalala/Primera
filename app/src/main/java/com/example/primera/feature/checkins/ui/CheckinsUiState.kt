package com.example.primera.feature.checkins.ui

import androidx.compose.ui.graphics.Color
import com.example.primera.feature.dashboard.ui.DashboardLogUiItem
import com.example.primera.feature.dashboard.ui.DashboardWeekDayItem

data class CheckinsOverviewUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val logs: List<DashboardLogUiItem> = emptyList(),
    val errorMessage: String? = null
)

data class DailyCheckinUiState(
    val isLoading: Boolean = false,
    val weekDays: List<DashboardWeekDayItem> = emptyList(),
    val availableSymptoms: List<CheckinOption> = emptyList(),
    val availableMoods: List<CheckinOption> = emptyList(),
    val availableMedicines: List<CheckinOption> = emptyList(),
    val selectedSymptoms: Set<String> = emptySet(),
    val selectedMoods: Set<String> = emptySet(),
    val selectedMedicines: Set<String> = emptySet(),
    val note: String = "",
    val weightKg: String = "",
    val lastWeightUpdateDate: String? = null,
    val shouldShowWeightUpdateAlert: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

data class CheckinOption(
    val label: String,
    val emoji: String,
    val category: String
)
