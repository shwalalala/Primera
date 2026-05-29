package com.example.primera.feature.dashboard.data

import java.util.Date

data class UserDto(
    val fullName: String? = null,
    val dueDate: Date? = null,
    val steps: Long? = null,
    val stepsGoal: Long? = null,
    val heartRateBpm: Long? = null,
    val sleepHours: Long? = null,
    val sleepMinutes: Long? = null,
    val spO2: Long? = null,
    val heightCm: Long? = null
)
