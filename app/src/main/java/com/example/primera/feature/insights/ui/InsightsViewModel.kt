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
import java.text.SimpleDateFormat
import java.util.*

class InsightsViewModel(
    private val dashboardRepository: DashboardRepository,
    private val checkinsRepository: CheckinsRepository,
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val _activePeriod = MutableStateFlow("Weekly")
    val activePeriod: StateFlow<String> = _activePeriod.asStateFlow()

    private val _weightPeriod = MutableStateFlow("Weekly")
    val weightPeriod: StateFlow<String> = _weightPeriod.asStateFlow()

    private val _weightOffset = MutableStateFlow(0)
    val weightOffset: StateFlow<Int> = _weightOffset.asStateFlow()

    private val _moodPeriod = MutableStateFlow("Weekly")
    val moodPeriod: StateFlow<String> = _moodPeriod.asStateFlow()

    private val _moodOffset = MutableStateFlow(0)
    val moodOffset: StateFlow<Int> = _moodOffset.asStateFlow()

    private val _activityPeriod = MutableStateFlow("Weekly")
    val activityPeriod: StateFlow<String> = _activityPeriod.asStateFlow()

    private val _activityOffset = MutableStateFlow(0)
    val activityOffset: StateFlow<Int> = _activityOffset.asStateFlow()

    private val _highlightedWeightIndex = MutableStateFlow(-1)
    private val _highlightedMoodIndex = MutableStateFlow(-1)
    private val _highlightedActivityIndex = MutableStateFlow(-1)

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
        combine(_activePeriod, _weightPeriod, _moodPeriod, _activityPeriod) { p -> p },
        combine(_weightOffset, _moodOffset, _activityOffset) { o -> o }
    ) { args ->
        val dashboardData = args[0] as com.example.primera.feature.dashboard.domain.DashboardData?
        val logs = args[1] as List<com.example.primera.feature.checkins.data.CheckinLogDto>
        val weightData = args[2] as com.example.primera.feature.checkins.data.CheckinUserDto?
        val goals = args[3] as List<com.example.primera.feature.goals.data.GoalDto>
        val periods = args[4] as Array<String>
        val offsets = args[5] as Array<Int>

        val activePeriod = periods[0]
        val weightPeriod = periods[1]
        val moodPeriod = periods[2]
        val activityPeriod = periods[3]
        
        val weightOffset = offsets[0]
        val moodOffset = offsets[1]
        val activityOffset = offsets[2]

        val filteredGoals = filterGoalsByPeriod(goals, activePeriod, 0)
        val filteredLogsForProgress = filterLogsByPeriod(logs, activePeriod, 0)

        // Goal counts should match the period wording
        val totalGoalsCount = filteredGoals.size
        val completedGoalsCount = filteredGoals.count { it.currentValue >= it.targetValue }
        
        // Progress Calculation
        val baseScore = if (totalGoalsCount > 0) (completedGoalsCount.toFloat() / totalGoalsCount) else 0f
        val checkinOffset = calculateCheckinOffset(filteredLogsForProgress)
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

        val healthWarning = generateHealthWarning(filteredLogsForProgress, activePeriod)

        val weightLogsForChart = filterLogsByPeriod(logs, weightPeriod, weightOffset)
        val weightValues = extractWeightValues(weightLogsForChart, weightData?.weightKg?.toFloat() ?: 0f)

        val moodLogsForChart = filterLogsByPeriod(logs, moodPeriod, moodOffset)
        val moodValues = extractMoodValues(moodLogsForChart)

        val activityGoals = filterGoalsByPeriod(goals, activityPeriod, activityOffset)
        val activityValues = extractActivityValues(activityGoals)

        // BMI Calculation
        val heightCm = dashboardData?.heightCm?.toFloat() ?: 0f
        val currentWeight = weightData?.weightKg?.toFloat() ?: 0f
        var bmiValue: Float? = null
        var bmiStatus: String? = null
        
        if (heightCm > 0 && currentWeight > 0) {
            val heightMeters = heightCm / 100f
            bmiValue = currentWeight / (heightMeters * heightMeters)
            bmiStatus = when {
                bmiValue < 18.5f -> "Underweight"
                bmiValue < 25f -> "Healthy"
                bmiValue < 30f -> "Overweight"
                else -> "Obese"
            }
        }

        InsightsUiState.Success(
            InsightsUiModel(
                physiqueProgress = finalProgress,
                physiqueSubtitle = if (finalProgress >= 1f) "100%" else "${(finalProgress * 100).toInt()}%",
                physiqueStatus = statusText,
                progressColor = progressColor,
                activePeriod = activePeriod,
                completedGoalsText = "This $activePeriod, you've completed $completedGoalsCount/$totalGoalsCount goals.",
                goalsCompletionTip = if (completedGoalsCount == totalGoalsCount && totalGoalsCount > 0) 
                    "You're doing great, Mom. Keep it up!" 
                    else getRandomGoalTip(),
                goalsCompletionTipColor = progressColor.copy(alpha = 0.8f),
                healthWarningTip = healthWarning,
                healthWarningTipColor = if (healthWarning != null) Color(0xFFCE93D8) else Color.Transparent,
                bmiValue = bmiValue,
                bmiStatus = bmiStatus,
                rawGoals = filteredGoals,
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
                    title = "Weight ($weightPeriod)",
                    dateRange = getDateRangeText(weightPeriod, weightOffset),
                    values = weightValues,
                    labels = getChartLabels(weightPeriod, weightOffset),
                    highlightedIndex = _highlightedWeightIndex.value
                ),
                moodTrend = ChartData(
                    title = "Mood ($moodPeriod)",
                    dateRange = getDateRangeText(moodPeriod, moodOffset),
                    values = moodValues,
                    labels = getChartLabels(moodPeriod, moodOffset),
                    highlightedIndex = _highlightedMoodIndex.value
                ),
                activityTrend = ChartData(
                    title = "Activity ($activityPeriod)",
                    dateRange = getDateRangeText(activityPeriod, activityOffset),
                    values = activityValues,
                    labels = getChartLabels(activityPeriod, activityOffset),
                    highlightedIndex = _highlightedActivityIndex.value
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
            val desc = log.description ?: ""
            // Parse "Symptoms: Back Pain; Mood: Happy; ..."
            if (desc.contains("Symptoms:")) {
                val symptomsPart = desc.substringAfter("Symptoms:").substringBefore(";")
                if (symptomsPart.isNotBlank() && !symptomsPart.contains("none", ignoreCase = true)) {
                    symptomsPart.split(",").forEach { 
                        val s = it.trim()
                        if (s.isNotBlank()) symptoms.add(s)
                    }
                }
            }
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

    private fun filterGoalsByPeriod(goals: List<com.example.primera.feature.goals.data.GoalDto>, period: String, offset: Int): List<com.example.primera.feature.goals.data.GoalDto> {
        val calendar = Calendar.getInstance()
        applyOffset(calendar, period, offset)
        val targetDate = calendar.time
        
        return goals.filter { goal ->
            val goalDate = goal.timestamp ?: Date()
            when (period) {
                "Daily" -> isSameDay(goalDate, targetDate)
                "Weekly" -> isSameWeek(goalDate, targetDate)
                "Monthly" -> isSameMonth(goalDate, targetDate)
                "Yearly" -> isSameYear(goalDate, targetDate)
                else -> true
            }
        }
    }

    private fun filterLogsByPeriod(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>, period: String, offset: Int): List<com.example.primera.feature.checkins.data.CheckinLogDto> {
        val calendar = Calendar.getInstance()
        applyOffset(calendar, period, offset)
        val targetDate = calendar.time

        return logs.filter { log ->
            val logDate = log.timestamp ?: Date()
            when (period) {
                "Daily" -> isSameDay(logDate, targetDate)
                "Weekly" -> isSameWeek(logDate, targetDate)
                "Monthly" -> isSameMonth(logDate, targetDate)
                "Yearly" -> isSameYear(logDate, targetDate)
                else -> true
            }
        }
    }

    private fun applyOffset(calendar: Calendar, period: String, offset: Int) {
        if (offset == 0) return
        when (period) {
            "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, offset)
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, offset)
            "Monthly" -> calendar.add(Calendar.MONTH, offset)
            "Yearly" -> calendar.add(Calendar.YEAR, offset)
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

    private fun getDateRangeText(period: String, offset: Int): String {
        val calendar = Calendar.getInstance()
        applyOffset(calendar, period, offset)
        
        return when (period) {
            "Daily" -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(calendar.time)
            "Weekly" -> {
                val end = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                val start = calendar.time
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                "${sdf.format(start)} - ${sdf.format(end)}"
            }
            "Monthly" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            "Yearly" -> SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
            else -> "Range"
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

    private fun extractActivityValues(goals: List<com.example.primera.feature.goals.data.GoalDto>): List<Float> {
        // Find "Walking" or "Yoga" or "Movement" goals
        val movementKeywords = listOf("walking", "yoga", "movement", "exercise", "steps", "reps", "min")
        val activityGoals = goals.filter { goal ->
            val title = goal.title?.lowercase() ?: ""
            movementKeywords.any { it in title }
        }
        
        return if (activityGoals.isEmpty()) {
            emptyList()
        } else {
            // If we have multiple, let's take the top 7 most recent ones
            activityGoals.sortedByDescending { it.timestamp }
                .take(7)
                .map { it.currentValue.toFloat() }
                .reversed()
        }
    }

    private fun getChartLabels(period: String, offset: Int): List<String> {
        val calendar = Calendar.getInstance()
        applyOffset(calendar, period, offset)
        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
        
        return when (period) {
            "Daily" -> listOf(sdf.format(calendar.time))
            "Weekly" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                (0..6).map {
                    val label = sdf.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    label
                }
            }
            "Monthly" -> (0..3).map { "W${it + 1}" }
            "Yearly" -> (0..11).map { "M${it + 1}" }
            else -> emptyList()
        }
    }

    private fun extractWeightValues(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>, latestWeight: Float): List<Float> {
        val weights = logs.mapNotNull { log ->
            val desc = log.description ?: ""
            if (desc.contains("Weight:")) {
                desc.substringAfter("Weight:").substringBefore("kg").trim().toFloatOrNull()
            } else null
        }
        return if (weights.isEmpty()) {
            if (latestWeight > 0) listOf(latestWeight) else emptyList()
        } else weights
    }

    private fun extractMoodValues(logs: List<com.example.primera.feature.checkins.data.CheckinLogDto>): List<Float> {
        return logs.map { log ->
            val moodPart = log.description?.substringAfter("Mood:")?.substringBefore(";")?.trim() ?: ""
            when {
                moodPart.contains("Happy", ignoreCase = true) -> 5f
                moodPart.contains("Normal", ignoreCase = true) -> 3f
                moodPart.contains("Sad", ignoreCase = true) -> 2f
                moodPart.contains("Angry", ignoreCase = true) -> 1f
                else -> 3f
            }
        }
    }

    fun onPeriodSelected(period: String) {
        _activePeriod.value = period
    }

    fun onWeightPeriodSelected(period: String) { _weightPeriod.value = period }
    fun onWeightPrevious() { _weightOffset.value -= 1 }
    fun onWeightNext() { _weightOffset.value += 1 }
    fun onWeightBarClick(index: Int) { _highlightedWeightIndex.value = index }

    fun onMoodPeriodSelected(period: String) { _moodPeriod.value = period }
    fun onMoodPrevious() { _moodOffset.value -= 1 }
    fun onMoodNext() { _moodOffset.value += 1 }
    fun onMoodBarClick(index: Int) { _highlightedMoodIndex.value = index }

    fun onActivityPeriodSelected(period: String) { _activityPeriod.value = period }
    fun onActivityPrevious() { _activityOffset.value -= 1 }
    fun onActivityNext() { _activityOffset.value += 1 }
    fun onActivityBarClick(index: Int) { _highlightedActivityIndex.value = index }
}
