
package cit.edu.primera.feature.checkins.ui

import cit.edu.primera.feature.dashboard.ui.DashboardLogUiItem
import cit.edu.primera.feature.dashboard.ui.DashboardWeekDayItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CheckinsOverviewUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val logs: List<DashboardLogUiItem> = emptyList(),
    val errorMessage: String? = null
)

data class DailyCheckinUiState(
    val editingId: String? = null,
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
    val success: Boolean = false,

    // Our additions: selectable check-in date and measurements
    val selectedCheckinDate: String = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date()),
    val bloodSugarMgDl: String = "",
    val originalTimestamp: Date? = null
)

data class CheckinOption(
    val label: String,
    val category: String,
    val emoji: String = ""
)
