package cit.edu.primera.feature.checkins.data

import cit.edu.primera.core.data.PreferenceRepository

import kotlinx.coroutines.flow.Flow

class CheckinsRepositoryImpl(
    private val dataSource: CheckinsDataSource,
    private val preferenceRepository: PreferenceRepository
) : CheckinsRepository {
    override fun observeLogs(): Flow<List<CheckinLogDto>> = dataSource.observeLogs()
    
    override fun observeUserWeight(): Flow<CheckinUserDto?> = dataSource.observeUserWeight()
    
    override suspend fun saveLog(log: CheckinLogDto): Result<Unit> = dataSource.saveLog(log)
    
    override suspend fun updateUserWeight(weightKg: Int): Result<Unit> = dataSource.updateUserWeight(weightKg)

    override fun getCustomOptions(category: String): Set<String> {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        return preferenceRepository.getCustomOptions(userId, category)
    }

    override fun addCustomOption(category: String, label: String) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        preferenceRepository.addCustomOption(userId, category, label)
    }
}
