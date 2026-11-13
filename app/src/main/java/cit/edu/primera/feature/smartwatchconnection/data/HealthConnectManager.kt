package cit.edu.primera.feature.smartwatchconnection.data

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
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectManager(
    private val context: Context
) {
    private fun getClient(): HealthConnectClient {
        if (!isPackageInstalled()) {
            throw Exception("Health Connect is not installed on this device.")
        }
        return try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: IllegalStateException) {
            throw Exception("Health Connect service is not available. Try opening the Health Connect app manually to initialize it.", e)
        } catch (e: Exception) {
            throw Exception("Could not initialize Health Connect: ${e.message}", e)
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

    fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    fun getAvailabilityStatus(): Int {
        return try {
            HealthConnectClient.getSdkStatus(context)
        } catch (e: Exception) {
            -1 // Unknown/Error state
        }
    }

    /**
     * Checks if Health Connect is available or installed on this device.
     * On Android 14+ (API 34+), Health Connect is a core Android system framework service
     * rather than a standalone APK download from Google Play.
     */
    fun isPackageInstalled(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val status = getAvailabilityStatus()
            return status != HealthConnectClient.SDK_UNAVAILABLE
        }
        return try {
            context.packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
            true
        } catch (_: Exception) {
            getAvailabilityStatus() == HealthConnectClient.SDK_AVAILABLE
        }
    }

    fun openHealthConnect() {
        try {
            // First attempt: Intent from HealthConnectClient (works on system framework and standalone)
            val manageIntent = HealthConnectClient.getHealthConnectManageDataIntent(context).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (manageIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(manageIntent)
                return
            }

            // Fallback for Android 13- standalone app
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
            if (launchIntent != null) {
                launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }

            // Direct settings action fallback
            val settingsIntent = android.content.Intent("androidx.health.action.HEALTH_CONNECT_SETTINGS").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (settingsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(settingsIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthConnectManager", "Could not open Health Connect", e)
        }
    }

    /**
     * Checks permissions. Returns true if granted, false if not, 
     * or throws an exception if the service is unreachable.
     */
    suspend fun hasAllPermissions(): Boolean {
        val grantedPermissions = getClient().permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(permissions)
    }

    suspend fun readTodaySmartwatchHealth(): SmartwatchHealth {
        val client = getClient()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        val startTime = today.atStartOfDay(zoneId).toInstant()
        val endTime = Instant.now()

        val aggregateResponse = client.aggregate(
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
            client = client,
            startTime = startTime,
            endTime = endTime
        )

        val latestSpO2 = readLatestSpO2(
            client = client,
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

    suspend fun readPastWeekSmartwatchHealth(): Map<LocalDate, SmartwatchHealth> {
        val client = getClient()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val result = mutableMapOf<LocalDate, SmartwatchHealth>()

        for (i in 0 until 7) {
            val date = today.minusDays(i.toLong())
            val startTime = date.atStartOfDay(zoneId).toInstant()
            val endTime = date.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)

            val aggregateResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        HeartRateRecord.BPM_AVG,
                        SleepSessionRecord.SLEEP_DURATION_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val avgHr = aggregateResponse[HeartRateRecord.BPM_AVG]
            val sleepDur = aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL]

            result[date] = SmartwatchHealth(
                date = date.toString(),
                averageHeartRate = avgHr,
                sleepMinutes = sleepDur?.toMinutes() ?: 0L
            )
        }
        return result
    }

    private suspend fun readLatestHeartRate(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Long? {
        val response = client.readRecords(
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
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Double? {
        val response = client.readRecords(
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
