package com.example.primera.feature.dashboard.data.repository

import com.example.primera.feature.dashboard.data.datasource.DashboardDataSource
import com.example.primera.feature.dashboard.domain.model.DashboardData
import com.example.primera.feature.dashboard.domain.model.DashboardHealthLog
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
}
