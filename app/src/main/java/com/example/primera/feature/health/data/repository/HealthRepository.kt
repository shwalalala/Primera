package com.example.primera.feature.health.data.repository

import com.example.primera.feature.health.domain.model.HealthLogModel
import com.example.primera.feature.health.domain.model.HealthStatsModel
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun observeHealthStats(): Flow<HealthStatsModel?>
    fun observeRecentLogs(): Flow<List<HealthLogModel>>
}
