package com.example.primera.feature.dashboard.domain

import java.util.Date

data class DashboardData(
    val userName: String,
    val dueDate: Date?,
    val steps: Int,
    val stepsGoal: Int,
    val heartRateBpm: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val spO2: Int? = null,
    val heightCm: Int? = null,
    val recentLogs: List<DashboardHealthLog>
)

data class DashboardHealthLog(
    val id: String? = null,
    val category: String,
    val description: String,
    val timestamp: Date
)
