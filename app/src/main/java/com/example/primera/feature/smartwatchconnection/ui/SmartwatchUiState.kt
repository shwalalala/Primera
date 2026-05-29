package com.example.primera.feature.smartwatchconnection.ui

import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth

data class SmartwatchUiState(
    val isLoading: Boolean = false,
    val message: String = "Please request Health Connect permissions.",
    val smartwatchHealth: SmartwatchHealth? = null,
    val hasPermissions: Boolean = false
)