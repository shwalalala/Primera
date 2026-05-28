package com.example.primera.feature.dashboard.data

import com.example.primera.feature.dashboard.domain.DashboardData
import com.example.primera.feature.dashboard.domain.DashboardHealthLog
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
                userName = userDto.fullName ?: "Sarah",
                dueDate = userDto.dueDate,
                steps = userDto.steps?.toInt() ?: 0,
                stepsGoal = userDto.stepsGoal?.toInt() ?: 8000,
                heartRateBpm = userDto.heartRateBpm?.toInt() ?: 72,
                sleepHours = userDto.sleepHours?.toInt() ?: 0,
                sleepMinutes = userDto.sleepMinutes?.toInt() ?: 0,
                heightCm = userDto.heightCm?.toInt(),
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
        sleepMinutes: Long
    ): Result<Unit> {
        return dataSource.updateHealthData(steps, heartRate, sleepHours, sleepMinutes)
    }
}
