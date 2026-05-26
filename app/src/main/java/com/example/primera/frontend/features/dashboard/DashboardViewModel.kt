package com.example.primera.frontend.features.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.Calendar

class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            userName = getCurrentUserName(),
            dueDate = getCurrentDueDate()
        )
    )
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun onViewAllLogs()  { /* navigate or load more */ }
    fun onAddLog()       { /* open add log sheet    */ }
    fun onInputManually(){ /* open manual input     */ }

    fun updateUserProfile(userName: String, dueDate: Date?) {
        _state.update { currentState ->
            currentState.copy(
                userName = userName,
                dueDate = dueDate
            )
        }
    }

    fun refreshPregnancyData() {
        _state.update { currentState ->
            currentState.copy(
                dueDate = currentState.dueDate
            )
        }
    }

    private fun getCurrentUserName(): String {
        return "Sarah"
    }

    private fun getCurrentDueDate(): Date? {
        // Return null for now to trigger default fallback logic in DashboardState
        return null
    }
}
