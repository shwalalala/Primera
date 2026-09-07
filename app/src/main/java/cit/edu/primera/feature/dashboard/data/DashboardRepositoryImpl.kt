package cit.edu.primera.feature.dashboard.data

import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.dashboard.domain.DashboardHealthLog
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.*

class DashboardRepositoryImpl(
    private val dataSource: DashboardDataSource
) : DashboardRepository {

    override fun observeDashboardData(): Flow<DashboardData?> {
        return combine(
            dataSource.observeUserProfile(),
            dataSource.observeRecentLogs()
        ) { userDto, logsDtoList ->
            if (userDto == null) return@combine null

            DashboardData(
                firstName = userDto.firstName,
                lastName = userDto.lastName,
                middleName = userDto.middleName,
                userName = userDto.username ?: userDto.fullName ?: userDto.firstName ?: "Sarah",
                birthday = userDto.birthday,
                lmpDate = userDto.lmpDate,
                dueDate = userDto.dueDate,
                steps = userDto.steps?.toInt() ?: 0,
                stepsGoal = userDto.stepsGoal?.toInt() ?: 8000,
                heartRateBpm = userDto.heartRateBpm?.toInt() ?: 0,
                sleepHours = userDto.sleepHours?.toInt() ?: 0,
                sleepMinutes = userDto.sleepMinutes?.toInt() ?: 0,
                spO2 = userDto.spO2?.toInt(),
                heightCm = userDto.heightCm?.toInt(),
                isCycleRegular = userDto.isCycleRegular,
                shortestCycleDays = userDto.shortestCycleDays?.toInt(),
                longestCycleDays = userDto.longestCycleDays?.toInt(),
                hasHadUltrasound = userDto.hasHadUltrasound,
                positiveTestDate = userDto.positiveTestDate,
                scanDate = userDto.scanDate,
                scanWeeks = userDto.scanWeeks?.toInt(),
                scanDays = userDto.scanDays?.toInt(),
                isFirstPregnancy = userDto.isFirstPregnancy,
                iceName = userDto.iceName,
                iceRelationship = userDto.iceRelationship,
                icePrimaryPhone = userDto.icePrimaryPhone,
                iceSecondaryPhone = userDto.iceSecondaryPhone,
                recentLogs = logsDtoList.map { dto ->
                    DashboardHealthLog(
                        id = dto.id,
                        category = dto.category ?: dto.type ?: "Check-in",
                        description = dto.description ?: dto.message ?: "",
                        timestamp = dto.timestamp ?: Date()
                    )
                }
            )
        }
    }

    override suspend fun updateStepsGoal(goal: Long): Result<Unit> {
        return dataSource.updateStepsGoal(goal)
    }

    override suspend fun updateHealthData(
        steps: Long,
        heartRate: Long,
        sleepHours: Long,
        sleepMinutes: Long,
        spO2: Long?
    ): Result<Unit> {
        return dataSource.updateHealthData(steps, heartRate, sleepHours, sleepMinutes, spO2)
    }

    override suspend fun saveHistoricalRecord(record: SmartwatchHealth): Result<Unit> {
        return dataSource.saveHistoricalRecord(record)
    }

    override fun observeHealthRecords(): Flow<List<SmartwatchHealth>> {
        return dataSource.observeHealthRecords()
    }
}
