package com.example.primera.feature.checkins.data

import java.util.Date

data class CheckinLogDto(
    val id: String? = null,
    val type: String? = null,
    val category: String? = null,
    val message: String? = null,
    val description: String? = null,
    val timestamp: Date? = null
)

data class CheckinUserDto(
    val weightKg: Int? = null,
    val weightUpdatedAt: Date? = null
)
