package com.example.primera.feature.goals.data

import java.util.Date

data class GoalDto(
    val id: String? = null,
    val userId: String? = null,
    val title: String? = null,
    val icon: String? = null,
    val currentValue: Double = 0.0,
    val targetValue: Double = 0.0,
    val unit: String? = null,
    val category: String? = null, // e.g., "Wellness", "Physical"
    val period: String? = "Weekly", // e.g., "Daily", "Weekly"
    val accentColorHex: String? = null,
    val timestamp: Date? = null
)
