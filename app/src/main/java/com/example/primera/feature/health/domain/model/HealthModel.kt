package com.example.primera.feature.health.domain.model

import java.util.Date

data class HealthStatsModel(
    val steps: Int,
    val stepsGoal: Int,
    val heartRateBpm: Int,
    val sleepHours: Int,
    val sleepMinutes: Int
)

data class HealthLogModel(
    val category: String,
    val message: String,
    val timestamp: Date
)
