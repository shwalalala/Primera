package com.example.primera.frontend.features.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.primera.data.repository.DashboardRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()
    private var userListener: ListenerRegistration? = null
    private var logsListener: ListenerRegistration? = null

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        startObservingData()
    }

    private fun startObservingData() {
        _state.update { it.copy(isLoading = true) }
        
        userListener = repository.observeUserProfile(
            onUpdate = { data ->
                val name = data["fullName"] as? String ?: "Sarah"
                val dueDate = (data["dueDate"] as? com.google.firebase.Timestamp)?.toDate()
                val steps = (data["steps"] as? Long)?.toInt() ?: 0
                val stepsGoal = (data["stepsGoal"] as? Long)?.toInt() ?: 8000
                val heartRate = (data["heartRateBpm"] as? Long)?.toInt() ?: 72
                val sleepH = (data["sleepHours"] as? Long)?.toInt() ?: 0
                val sleepM = (data["sleepMinutes"] as? Long)?.toInt() ?: 0

                _state.update { currentState ->
                    currentState.copy(
                        userName = name,
                        dueDate = dueDate,
                        steps = steps,
                        stepsGoal = stepsGoal,
                        heartRateBpm = heartRate,
                        sleepHours = sleepH,
                        sleepMinutes = sleepM,
                        sleepQuality = DashboardState.getSleepQuality(sleepH, sleepM),
                        // Recalculate derived fields
                        trimester = DashboardState.getTrimester(dueDate),
                        weekNumber = DashboardState.getWeekNumber(dueDate),
                        dayNumber = DashboardState.getDayNumber(dueDate),
                        daysLeft = DashboardState.getDaysLeft(dueDate),
                        babySize = DashboardState.getBabySize(DashboardState.getWeekNumber(dueDate)),
                        isLoading = false
                    )
                }
            },
            onError = {
                _state.update { it.copy(isLoading = false) }
            }
        )

        logsListener = repository.observeRecentLogs { logDataList ->
            val logs = logDataList.map { data ->
                val category = (data["type"] as? String) ?: (data["category"] as? String) ?: "Other"
                val message = (data["message"] as? String) ?: (data["description"] as? String) ?: ""
                val timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                
                HealthLogItem(
                    category = category,
                    description = message,
                    time = formatTime(timestamp),
                    accentColor = getCategoryColor(category)
                )
            }
            _state.update { it.copy(recentLogs = if (logs.isEmpty()) defaultLogs else logs) }
        }
    }

    private fun formatTime(date: Date): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain" -> Color(0xFFEF9A9A)
            "nutrition", "food" -> Color(0xFFA5D6A7)
            "fetal movement", "baby" -> Color(0xFF9FA8DA)
            else -> Color(0xFFB0BEC5)
        }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        logsListener?.remove()
    }

    fun onViewAllLogs()  { /* navigate or load more */ }
    fun onAddLog()       { /* open add log sheet    */ }
    fun onInputManually(){ /* open manual input     */ }
}
