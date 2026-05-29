package com.example.primera.feature.smartwatchconnection.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectManager(
    private val context: Context
) {
    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) ==
                HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val grantedPermissions =
            healthConnectClient.permissionController.getGrantedPermissions()

        return grantedPermissions.containsAll(permissions)
    }

    suspend fun readTodaySmartwatchHealth(): SmartwatchHealth {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        val startTime = today.atStartOfDay(zoneId).toInstant()
        val endTime = Instant.now()

        val aggregateResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    HeartRateRecord.BPM_AVG,
                    HeartRateRecord.BPM_MIN,
                    HeartRateRecord.BPM_MAX,
                    SleepSessionRecord.SLEEP_DURATION_TOTAL
                ),
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        val currentHeartRate = readLatestHeartRate(
            startTime = startTime,
            endTime = endTime
        )

        val latestSpO2 = readLatestSpO2(
            startTime = startTime,
            endTime = endTime
        )

        val steps = aggregateResponse[StepsRecord.COUNT_TOTAL] ?: 0L
        val averageHeartRate = aggregateResponse[HeartRateRecord.BPM_AVG]
        val minimumHeartRate = aggregateResponse[HeartRateRecord.BPM_MIN]
        val maximumHeartRate = aggregateResponse[HeartRateRecord.BPM_MAX]
        val sleepDuration = aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL]

        return SmartwatchHealth(
            date = today.toString(),
            steps = steps,
            currentHeartRate = currentHeartRate,
            averageHeartRate = averageHeartRate,
            minimumHeartRate = minimumHeartRate,
            maximumHeartRate = maximumHeartRate,
            spO2 = latestSpO2,
            sleepMinutes = sleepDuration?.toMinutes() ?: 0L,
            syncedAt = System.currentTimeMillis()
        )
    }

    private suspend fun readLatestHeartRate(
        startTime: Instant,
        endTime: Instant
    ): Long? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )

        return response.records
            .flatMap { record -> record.samples }
            .maxByOrNull { sample -> sample.time }
            ?.beatsPerMinute
    }

    private suspend fun readLatestSpO2(
        startTime: Instant,
        endTime: Instant
    ): Double? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = OxygenSaturationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )

        return response.records
            .maxByOrNull { record -> record.time }
            ?.percentage
            ?.value
    }
}