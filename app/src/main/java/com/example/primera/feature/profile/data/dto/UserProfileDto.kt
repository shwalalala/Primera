package com.example.primera.feature.profile.data.dto

import com.google.firebase.Timestamp

data class UserProfileDto(
    val fullName: String? = null,
    val dueDate: Timestamp? = null,
    val steps: Long? = null,
    val stepsGoal: Long? = null,
    val heartRateBpm: Long? = null,
    val sleepHours: Long? = null,
    val sleepMinutes: Long? = null,
    val email: String? = null,
    val createdAt: Timestamp? = null
)
