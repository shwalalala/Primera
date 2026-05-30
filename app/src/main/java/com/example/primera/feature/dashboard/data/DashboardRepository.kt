package com.example.primera.feature.dashboard.data

import com.example.primera.feature.dashboard.domain.DashboardData
import com.example.primera.feature.dashboard.domain.DashboardHealthLog
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardData(): Flow<DashboardData?>
    suspend fun updateStepsGoal(goal: Long): Result<Unit>
    suspend fun updateHealthData(steps: Long, heartRate: Long, sleepHours: Long, sleepMinutes: Long, spO2: Long? = null): Result<Unit>
}
