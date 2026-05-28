package com.example.primera.feature.insights.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.graphics.toColorInt
import com.example.primera.feature.checkins.data.CheckinsRepository
import com.example.primera.feature.dashboard.data.DashboardRepository
import com.example.primera.feature.goals.data.GoalsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class InsightsViewModel(
    private val dashboardRepository: DashboardRepository,
    private val checkinsRepository: CheckinsRepository,
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val _activePeriod = MutableStateFlow("Weekly")
    val activePeriod: StateFlow<String> = _activePeriod.asStateFlow()

    init {
        viewModelScope.launch {
            goalsRepository.ensureMandatoryGoals()
        }
    }

    val uiState: StateFlow<InsightsUiState> = combine(
        dashboardRepository.observeDashboardData(),
        checkinsRepository.observeLogs(),
        checkinsRepository.observeUserWeight(),
        goalsRepository.observeGoals(),
        _activePeriod
    ) { dashboardData, logs, weightData, goals, period ->
        
        val filteredGoals = filterGoalsByPeriod(goals, period)
        val filteredLogs = filterLogsByPeriod(logs, period)

        // Goal counts should match the period wording
        val totalGoalsCount = filteredGoals.size
        val completedGoalsCount = filteredGoals.count { it.currentValue >= it.targetValue }
        
        // Progress Calculation
        val baseScore = if (totalGoalsCount > 0) (completedGoalsCount.toFloat() / totalGoalsCount) else 0f
        val checkinOffset = calculateCheckinOffset(filteredLogs)
        val finalProgress = (baseScore + checkinOffset).coerceIn(0f, 1f)

        val statusText = when {
            finalProgress >= 0.6f -> "Healthy"
            finalProgress >= 0.4f -> "Moderate"
            else -> "Bad"
        }

        val progressColor = when (statusText) {
            "Healthy" -> Color(0xFFA1D386)
            "Moderate" -> Color(0xFFFBBB67)
            else -> Color(0xFFF28B82)
        }

        val healthWarning = generateHealthWarning(filteredLogs, period)

        InsightsUiState.Success(
            InsightsUiModel(
                physiqueProgress = finalProgress,
                physiqueSubtitle = if (finalProgress >= 1f) "100%" else "${(finalProgress * 100).toInt()}%",
                physiqueStatus = statusText,
                progressColor = progressColor,
                activePeriod = period,
                completedGoalsText = "This $period, you've completed $completedGoalsCount/$totalGoalsCount goals.",
                goalsCompletionTip = if (completedGoalsCount == totalGoalsCount && totalGoalsCount > 0) 
                    "You're doing great, Mom. Keep it up!" 
                    else getRandomGoalTip(),
                goalsCompletionTipColor = progressColor.copy(alpha = 0.8f),
                healthWarningTip = healthWarning,
                healthWarningTipColor = if (healthWarning != null) Color(0xFFCE93D8) else Color.Transparent,
                wellnessGoals = filteredGoals.map { goal ->
                    InsightGoalUiItem(
                        id = goal.id ?: "",
                        icon = goal.icon ?: "🎯",
                        title = goal.title ?: "Goal",
                        current = String.format(Locale.getDefault(), "%.1f", goal.currentValue),
                        target = String.format(Locale.getDefault(), "%.1f %s", goal.targetValue, goal.unit ?: ""),
                        progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f,
                        color = goal.accentColorHex?.let { parseHexColor(it) } ?: Color(0xFF64B5F6)
                    )
                },
                weightTrend = ChartData(
                    title = "Weight(kg)",
                    dateRange = getDateRangeText(period),
                    values = extractWeightValues(weightData?.weightKg?.toFloat() ?: 0f)
                ),
                moodTrend = ChartData(
                    title = "Mood Trend",
                    dateRange = getDateRangeText(period),
                    values = extractMoodValues(filteredLogs)
                ),
                activityTrend = ChartData(
                    title = "This week's activity",
                    dateRange = getDateRangeText(period),
                    values = getDummyActivityValues(period),
                    highlightedIndex = 6
                )
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState.Loading
    )

    private fun getRandomGoalTip(): String {
        val tips = listOf(
            "Try focusing on one small goal each day this week.",
            "Stay consistent with your routine for better results.",
            "Even small steps lead to big progress. You got this!",
            "Prioritize your hydration today, it's essential for your energy levels.",
            "Take a short walk to clear your mind and hit your movement goal."
        )
        return tips.random()
    }

    private fun generateHealthWarning(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>, period: String): String? {
        val symptoms = mutableListOf<String>()
        logs.forEach { log ->
            val desc = log.description?.lowercase() ?: ""
            if (desc.contains("headache") || desc.contains("aching head")) symptoms.add("headaches")
            if (desc.contains("nausea")) symptoms.add("nausea")
            if (desc.contains("back pain")) symptoms.add("back pain")
        }
        
        if (symptoms.isEmpty()) return null
        
        val distinctSymptoms = symptoms.distinct()
        val symptomText = when (distinctSymptoms.size) {
            1 -> distinctSymptoms[0]
            2 -> "${distinctSymptoms[0]} and ${distinctSymptoms[1]}"
            else -> "${distinctSymptoms.dropLast(1).joinToString(", ")}, and ${distinctSymptoms.last()}"
        }
        
        val periodWording = when (period) {
            "Daily" -> "today"
            "Weekly" -> "this week"
            "Monthly" -> "this month"
            "Yearly" -> "this year"
            else -> "this period"
        }

        return "This $periodWording, you reported $symptomText. Monitor your symptoms closely and prioritize rest and hydration. If they continue for more than 3 days, consult your doctor."
    }

    private fun filterGoalsByPeriod(goals: List<com.example.primera.feature.goals.data.GoalDto>, period: String): List<com.example.primera.feature.goals.data.GoalDto> {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        return goals.filter { goal ->
            val goalDate = goal.timestamp ?: now
            when (period) {
                "Daily" -> isSameDay(goalDate, now)
                "Weekly" -> isSameWeek(goalDate, now)
                "Monthly" -> isSameMonth(goalDate, now)
                "Yearly" -> isSameYear(goalDate, now)
                else -> true
            }
        }
    }

    private fun filterLogsByPeriod(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>, period: String): List<com.example.primera.feature.checkins.data.CheckinLogDto> {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        return logs.filter { log ->
            val logDate = log.timestamp ?: now
            when (period) {
                "Daily" -> isSameDay(logDate, now)
                "Weekly" -> isSameWeek(logDate, now)
                "Monthly" -> isSameMonth(logDate, now)
                "Yearly" -> isSameYear(logDate, now)
                else -> true
            }
        }
    }

    private fun calculateCheckinOffset(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>): Float {
        var offset = 0f
        logs.forEach { log ->
            val desc = log.description?.lowercase() ?: ""
            // Positive
            if (desc.contains("happy") || desc.contains("normal")) offset += 0.05f
            // Negative 
            if (desc.contains("angry") || desc.contains("sad") || desc.contains("nausea") || 
                desc.contains("aching head") || desc.contains("back pain")) offset -= 0.05f
        }
        return offset
    }

    private fun getDateRangeText(period: String): String {
        return when (period) {
            "Daily" -> "Today"
            "Weekly" -> "Current Week"
            "Monthly" -> "Current Month"
            "Yearly" -> "2024"
            else -> "Range"
        }
    }

    private fun getDummyActivityValues(period: String): List<Float> {
        return when (period) {
            "Daily" -> listOf(2000f, 4000f, 6000f, 8000f)
            else -> listOf(2000f, 4000f, 6000f, 8000f, 7000f, 5000f, 6432f)
        }
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
    }

    private fun isSameYear(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }

    private fun parseHexColor(hex: String): Color {
        return try {
            Color(hex.toColorInt())
        } catch (_: Exception) {
            Color(0xFF64B5F6)
        }
    }

    private fun extractWeightValues(currentWeight: Float): List<Float> {
        return if (currentWeight > 0) listOf(currentWeight - 0.5f, currentWeight - 0.3f, currentWeight - 0.1f, currentWeight, currentWeight + 0.2f, currentWeight + 0.4f, currentWeight) else emptyList()
    }

    private fun extractMoodValues(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>): List<Float> {
        val moodValues = logs.filter { it.category == "Mood" || it.description?.contains("Mood:") == true }
            .map { log ->
                when {
                    log.description?.contains("Happy") == true -> 5f
                    log.description?.contains("Normal") == true -> 3f
                    log.description?.contains("Sad") == true -> 2f
                    log.description?.contains("Angry") == true -> 1f
                    else -> 3f
                }
            }
        return if (moodValues.isEmpty()) listOf(3f, 4f, 3f, 5f, 4f, 3f, 4f) else moodValues.take(7).reversed()
    }

    fun onPeriodSelected(period: String) {
        _activePeriod.value = period
    }

    fun onPreviousRange() {}
    fun onNextRange() {}
}
