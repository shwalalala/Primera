package com.example.primera.feature.dashboard.domain.model

import java.util.Date

data class DashboardData(
    val userName: String,
    val dueDate: Date?,
    val steps: Int,
    val stepsGoal: Int,
    val heartRateBpm: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val recentLogs: List<DashboardHealthLog>
)

data class DashboardHealthLog(
    val id: String? = null,
    val category: String,
    val description: String,
    val timestamp: Date
)
