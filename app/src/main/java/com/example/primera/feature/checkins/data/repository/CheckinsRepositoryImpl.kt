package com.example.primera.feature.checkins.data.repository

import com.example.primera.core.data.repository.PreferenceRepository
import com.example.primera.feature.checkins.data.datasource.CheckinsDataSource
import com.example.primera.feature.checkins.data.dto.CheckinLogDto
import com.example.primera.feature.checkins.data.dto.CheckinUserDto
import com.example.primera.feature.checkins.data.repository.CheckinsRepository
import kotlinx.coroutines.flow.Flow

class CheckinsRepositoryImpl(
    private val dataSource: CheckinsDataSource,
    private val preferenceRepository: PreferenceRepository
) : CheckinsRepository {
    override fun observeLogs(): Flow<List<CheckinLogDto>> = dataSource.observeLogs()
    
    override fun observeUserWeight(): Flow<CheckinUserDto?> = dataSource.observeUserWeight()
    
    override suspend fun saveLog(log: CheckinLogDto): Result<Unit> = dataSource.saveLog(log)
    
    override suspend fun updateUserWeight(weightKg: Int): Result<Unit> = dataSource.updateUserWeight(weightKg)

    override fun getCustomOptions(category: String): Set<String> = preferenceRepository.getCustomOptions(category)

    override fun addCustomOption(category: String, label: String) = preferenceRepository.addCustomOption(category, label)
}
