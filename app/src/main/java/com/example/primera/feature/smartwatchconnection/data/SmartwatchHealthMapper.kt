package com.example.primera.feature.smartwatchconnection.data


import com.example.primera.feature.smartwatchconnection.data.SmartwatchHealthDto
import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth

fun SmartwatchHealth.toDto(): SmartwatchHealthDto {
    return SmartwatchHealthDto(
        date = date,
        steps = steps,
        currentHeartRate = currentHeartRate,
        averageHeartRate = averageHeartRate,
        minimumHeartRate = minimumHeartRate,
        maximumHeartRate = maximumHeartRate,
        spO2 = spO2,
        sleepMinutes = sleepMinutes,
        syncedAt = syncedAt
    )
}

fun SmartwatchHealthDto.toDomain(): SmartwatchHealth {
    return SmartwatchHealth(
        date = date,
        steps = steps,
        currentHeartRate = currentHeartRate,
        averageHeartRate = averageHeartRate,
        minimumHeartRate = minimumHeartRate,
        maximumHeartRate = maximumHeartRate,
        spO2 = spO2,
        sleepMinutes = sleepMinutes,
        syncedAt = syncedAt
    )
}