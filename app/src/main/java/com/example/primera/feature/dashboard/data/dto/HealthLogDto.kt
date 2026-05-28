package com.example.primera.feature.dashboard.data.dto

import java.util.Date

data class HealthLogDto(
    val id: String? = null,
    val type: String? = null,
    val category: String? = null,
    val message: String? = null,
    val description: String? = null,
    val timestamp: Date? = null
)
