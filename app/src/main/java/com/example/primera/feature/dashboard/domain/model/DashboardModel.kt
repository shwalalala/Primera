package com.example.primera.feature.dashboard.domain.model

data class PregnancyInfo(
    val trimester: String,
    val weekNumber: Int,
    val dayNumber: Int,
    val daysLeft: Int,
    val babySize: String
)

data class DashboardData(
    val userName: String,
    val pregnancyInfo: PregnancyInfo?
)
