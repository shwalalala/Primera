package com.example.primera.feature.checkins.data
import kotlinx.coroutines.flow.Flow

interface CheckinsRepository {
    fun observeLogs(): Flow<List<CheckinLogDto>>
    fun observeUserWeight(): Flow<CheckinUserDto?>
    suspend fun saveLog(log: CheckinLogDto): Result<Unit>
    suspend fun updateUserWeight(weightKg: Int): Result<Unit>
    fun getCustomOptions(category: String): Set<String>
    fun addCustomOption(category: String, label: String)
}