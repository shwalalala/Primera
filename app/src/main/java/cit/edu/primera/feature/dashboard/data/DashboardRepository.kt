package cit.edu.primera.feature.dashboard.data

import cit.edu.primera.feature.dashboard.domain.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardData(): Flow<DashboardData?>
    suspend fun updateStepsGoal(goal: Long): Result<Unit>
    suspend fun updateHealthData(steps: Long, heartRate: Long, sleepHours: Long, sleepMinutes: Long, spO2: Long? = null): Result<Unit>
    suspend fun saveHistoricalRecord(record: cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth): Result<Unit>
    fun observeHealthRecords(): Flow<List<cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth>>
}
