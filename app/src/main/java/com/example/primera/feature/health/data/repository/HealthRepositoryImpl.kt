package com.example.primera.feature.health.data.repository

import com.example.primera.feature.auth.data.datasource.AuthDataSource
import com.example.primera.feature.health.data.datasource.HealthDataSource
import com.example.primera.feature.health.domain.model.HealthLogModel
import com.example.primera.feature.health.domain.model.HealthStatsModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthDataSource: HealthDataSource,
    private val authDataSource: AuthDataSource
) : HealthRepository {

    override fun observeHealthStats(): Flow<HealthStatsModel?> {
        val userId = authDataSource.getCurrentUserId() ?: return flowOf(null)
        return healthDataSource.observeHealthStats(userId).map { dto ->
            dto?.let {
                HealthStatsModel(
                    steps = it.steps?.toInt() ?: 0,
                    stepsGoal = it.stepsGoal?.toInt() ?: 8000,
                    heartRateBpm = it.heartRateBpm?.toInt() ?: 72,
                    sleepHours = it.sleepHours?.toInt() ?: 0,
                    sleepMinutes = it.sleepMinutes?.toInt() ?: 0
                )
            }
        }
    }

    override fun observeRecentLogs(): Flow<List<HealthLogModel>> {
        val userId = authDataSource.getCurrentUserId() ?: return flowOf(emptyList())
        return healthDataSource.observeRecentLogs(userId).map { list ->
            list.map { dto ->
                HealthLogModel(
                    category = dto.type ?: dto.category ?: "Other",
                    message = dto.message ?: dto.description ?: "",
                    timestamp = dto.timestamp?.toDate() ?: Date()
                )
            }
        }
    }
}
