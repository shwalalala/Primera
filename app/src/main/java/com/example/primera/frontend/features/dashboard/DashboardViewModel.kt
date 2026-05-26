package com.example.primera.frontend.features.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun onViewAllLogs()  { /* navigate or load more */ }
    fun onAddLog()       { /* open add log sheet    */ }
    fun onInputManually(){ /* open manual input     */ }
}