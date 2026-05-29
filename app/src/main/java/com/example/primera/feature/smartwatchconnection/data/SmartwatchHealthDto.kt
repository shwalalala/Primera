package com.example.primera.feature.smartwatchconnection.data

data class SmartwatchHealthDto(
    val date: String = "",
    val steps: Long = 0,

    val currentHeartRate: Long? = null,
    val averageHeartRate: Long? = null,
    val minimumHeartRate: Long? = null,
    val maximumHeartRate: Long? = null,

    val spO2: Double? = null,

    val sleepMinutes: Long = 0,
    val syncedAt: Long = System.currentTimeMillis()
)