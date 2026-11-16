package cit.edu.primera.feature.smartwatchconnection.data


import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth

fun SmartwatchHealth.toDto(): SmartwatchHealthDto {
    return SmartwatchHealthDto(
        date = date,
        time = time,
        steps = steps,
        currentHeartRate = currentHeartRate,
        averageHeartRate = averageHeartRate,
        minimumHeartRate = minimumHeartRate,
        maximumHeartRate = maximumHeartRate,
        spO2 = spO2,
        sleepMinutes = sleepMinutes,
        syncedAt = syncedAt,
        isManualEntry = isManualEntry,
        source = source
    )
}

fun SmartwatchHealthDto.toDomain(): SmartwatchHealth {
    return SmartwatchHealth(
        date = date,
        time = time,
        steps = steps,
        currentHeartRate = currentHeartRate,
        averageHeartRate = averageHeartRate,
        minimumHeartRate = minimumHeartRate,
        maximumHeartRate = maximumHeartRate,
        spO2 = spO2,
        sleepMinutes = sleepMinutes,
        syncedAt = syncedAt,
        isManualEntry = isManualEntry,
        source = source
    )
}