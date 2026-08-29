package com.example.primera.feature.smartwatchconnection.ui

import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth

data class SmartwatchUiState(
    val isLoading: Boolean = false,
    val message: String = "Please request Health Connect permissions.",
    val smartwatchHealth: SmartwatchHealth? = null,
    val hasPermissions: Boolean = false,
    val isPackageInstalled: Boolean = false,
    val isDataVisible: Boolean = false,
    val isOnline: Boolean = true,
    val bpmHistory: List<Float> = emptyList(),
    val sleepHistory: List<Float> = emptyList(),
    val historyLabels: List<String> = emptyList(),
    val hrTrendText: String = "",
    val hrTrendColor: Long = 0xFF757575, // Default grey
    val stepsTrendText: String = "Goal: 8,000",
    val sleepTrendText: String = "",
    val sleepTrendColor: Long = 0xFF757575,
    val spO2TrendText: String = "",
    val spO2TrendColor: Long = 0xFF757575
)