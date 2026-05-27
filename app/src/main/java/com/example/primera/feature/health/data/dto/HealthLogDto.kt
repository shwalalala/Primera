package com.example.primera.feature.health.data.dto

import com.google.firebase.Timestamp

data class HealthLogDto(
    val userId: String? = null,
    val type: String? = null,
    val category: String? = null,
    val message: String? = null,
    val description: String? = null,
    val timestamp: Timestamp? = null
)
