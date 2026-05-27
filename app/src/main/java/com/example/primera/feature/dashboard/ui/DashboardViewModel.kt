package com.example.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.utils.PregnancyCalculator
import com.example.primera.feature.health.data.repository.HealthRepository
import com.example.primera.feature.profile.data.repository.ProfileRepository
import com.example.primera.feature.wellness.data.repository.WellnessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val healthRepository: HealthRepository,
    private val wellnessRepository: WellnessRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        _state.update { it.copy(isLoading = true, timeOfDay = getTimeOfDay()) }

        profileRepository.observeUserProfile()
            .onEach { profile ->
                profile?.let {
                    val week = PregnancyCalculator.getWeekNumber(it.dueDate)
                    _state.update { currentState ->
                        currentState.copy(
                            userName = it.fullName,
                            trimester = PregnancyCalculator.getTrimester(week),
                            weekNumber = week,
                            dayNumber = PregnancyCalculator.getDayNumber(it.dueDate),
                            daysLeft = PregnancyCalculator.getDaysLeft(it.dueDate),
                            babySize = PregnancyCalculator.getBabySize(week),
                            isLoading = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        healthRepository.observeHealthStats()
            .onEach { stats ->
                _state.update { it.copy(
                    healthStats = stats,
                    sleepQuality = getSleepQuality(stats?.sleepHours ?: 0, stats?.sleepMinutes ?: 0)
                ) }
            }
            .launchIn(viewModelScope)

        healthRepository.observeRecentLogs()
            .onEach { logs ->
                val uiLogs = logs.map {
                    DashboardLogUiModel(
                        category = it.category,
                        message = it.message,
                        time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(it.timestamp),
                        color = getCategoryColor(it.category)
                    )
                }
                _state.update { it.copy(recentLogs = uiLogs) }
            }
            .launchIn(viewModelScope)

        wellnessRepository.observeWellnessGoals()
            .onEach { goals ->
                val uiGoals = goals.map {
                    DashboardWellnessGoalUiModel(
                        label = it.label,
                        emoji = it.emoji,
                        current = it.current,
                        target = it.target,
                        unit = it.unit,
                        color = Color(android.graphics.Color.parseColor("#${it.hexColor}"))
                    )
                }
                _state.update { it.copy(wellnessGoals = uiGoals) }
            }
            .launchIn(viewModelScope)
    }

    private fun getTimeOfDay(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    private fun getSleepQuality(hours: Int, minutes: Int): String {
        val totalMinutes = hours * 60 + minutes
        return when {
            totalMinutes >= 8 * 60 -> "Excellent quality"
            totalMinutes >= 7 * 60 -> "Good quality"
            totalMinutes >= 6 * 60 -> "Fair quality"
            else -> "Poor quality"
        }
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain" -> Color(0xFFEF9A9A)
            "nutrition", "food" -> Color(0xFFA5D6A7)
            "fetal movement", "baby" -> Color(0xFF9FA8DA)
            else -> Color(0xFFB0BEC5)
        }
    }

    fun onViewAllLogs() { /* Navigation handled in UI */ }
    fun onAddLog() { /* UI logic */ }
    fun onInputManually() { /* UI logic */ }
}
