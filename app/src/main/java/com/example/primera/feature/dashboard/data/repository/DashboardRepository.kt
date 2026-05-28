package com.example.primera.feature.dashboard.data.repository

import com.example.primera.feature.dashboard.domain.model.DashboardData
import com.example.primera.feature.dashboard.domain.model.DashboardHealthLog
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardData(): Flow<DashboardData?>
    suspend fun updateStepsGoal(goal: Long): Result<Unit>
    suspend fun updateHealthData(steps: Long, heartRate: Long, sleepHours: Long, sleepMinutes: Long): Result<Unit>
}
