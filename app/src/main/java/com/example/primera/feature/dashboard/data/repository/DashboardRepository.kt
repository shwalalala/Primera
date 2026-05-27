package com.example.primera.feature.dashboard.data.repository

import com.example.primera.feature.dashboard.domain.model.DashboardData
import com.example.primera.feature.dashboard.domain.model.DashboardHealthLog
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardData(): Flow<DashboardData?>
}
