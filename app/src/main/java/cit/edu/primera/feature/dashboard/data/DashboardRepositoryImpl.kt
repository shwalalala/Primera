package cit.edu.primera.feature.dashboard.data

import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.dashboard.domain.DashboardHealthLog
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Date

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
                email = userDto.email,
                firstName = userDto.firstName,
                lastName = userDto.lastName,
                middleName = userDto.middleName,
                userName = userDto.username ?: userDto.fullName ?: userDto.firstName ?: "Sarah",
                photoUri = userDto.photoUri,
                birthday = userDto.birthday,
                lmpDate = userDto.lmpDate,
                dueDate = userDto.dueDate,
                steps = userDto.steps?.toInt() ?: 0,
                stepsGoal = userDto.stepsGoal?.toInt() ?: 8000,
                heartRateBpm = userDto.heartRateBpm?.toInt() ?: 0,
                sleepHours = userDto.sleepHours?.toInt() ?: 0,
                sleepMinutes = userDto.sleepMinutes?.toInt() ?: 0,
                spO2 = userDto.spO2?.toInt(),
                weightKg = userDto.weightKg?.toInt(),
                heightCm = userDto.heightCm?.toInt(),
                isCycleRegular = userDto.isCycleRegular,
                shortestCycleDays = userDto.shortestCycleDays?.toInt(),
                longestCycleDays = userDto.longestCycleDays?.toInt(),
                hasHadUltrasound = userDto.hasHadUltrasound,
                positiveTestDate = userDto.positiveTestDate,
                scanDate = userDto.scanDate,
                scanWeeks = userDto.scanWeeks?.toInt(),
                scanDays = userDto.scanDays?.toInt(),
                pregnancyHistories = userDto.pregnancyHistories?.map { map ->
                    cit.edu.primera.feature.onboarding.domain.PregnancyHistory(
                        pregnancyNumber = cit.edu.primera.core.util.CryptoManager.decryptInt(map["pregnancyNumber"])
                            ?: (map["pregnancyNumber"] as? Number)?.toInt() ?: 1,
                        deliveryDate = cit.edu.primera.core.util.CryptoManager.decryptDate(map["deliveryDate"]),
                        deliveryType = cit.edu.primera.core.util.CryptoManager.decrypt(map["deliveryType"] as? String)
                            ?: map["deliveryType"] as? String ?: "",
                        birthOutcome = cit.edu.primera.core.util.CryptoManager.decrypt(map["birthOutcome"] as? String)
                            ?: map["birthOutcome"] as? String ?: "",
                        childrenDelivered = cit.edu.primera.core.util.CryptoManager.decrypt(map["childrenDelivered"] as? String)
                            ?: map["childrenDelivered"] as? String ?: "",
                        areChildrenLiving = cit.edu.primera.core.util.CryptoManager.decryptBoolean(
                            map["areChildrenLiving"]
                        )
                            ?: map["areChildrenLiving"] as? Boolean ?: true,
                        hasGdm = cit.edu.primera.core.util.CryptoManager.decryptBoolean(map["hasGdm"])
                            ?: map["hasGdm"] as? Boolean ?: false,
                        hasDiabetes = cit.edu.primera.core.util.CryptoManager.decryptBoolean(map["hasDiabetes"])
                            ?: map["hasDiabetes"] as? Boolean ?: false,
                        hasPreeclampsia = cit.edu.primera.core.util.CryptoManager.decryptBoolean(map["hasPreeclampsia"])
                            ?: map["hasPreeclampsia"] as? Boolean ?: false,
                        hasPretermDelivery = cit.edu.primera.core.util.CryptoManager.decryptBoolean(
                            map["hasPretermDelivery"]
                        )
                            ?: map["hasPretermDelivery"] as? Boolean ?: false,
                        additionalComplications = cit.edu.primera.core.util.CryptoManager.decrypt(
                            map["additionalComplications"] as? String
                        )
                            ?: map["additionalComplications"] as? String ?: "",
                        complications = cit.edu.primera.core.util.CryptoManager.decryptStringList(
                            map["complications"]
                        )
                            ?: (map["complications"] as? List<*>)?.mapNotNull { it?.toString() }
                            ?: emptyList()
                    )
                } ?: emptyList(),
                isFirstPregnancy = userDto.isFirstPregnancy,
                iceName = userDto.iceName,
                iceRelationship = userDto.iceRelationship,
                icePrimaryPhone = userDto.icePrimaryPhone,
                iceSecondaryPhone = userDto.iceSecondaryPhone,
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

    override suspend fun updateStepsGoal(goal: Long): Result<Unit> {
        return dataSource.updateStepsGoal(goal)
    }

    override suspend fun updateHealthData(
        steps: Long,
        heartRate: Long,
        sleepHours: Long,
        sleepMinutes: Long,
        spO2: Long?
    ): Result<Unit> {
        return dataSource.updateHealthData(steps, heartRate, sleepHours, sleepMinutes, spO2)
    }

    override suspend fun saveHistoricalRecord(record: SmartwatchHealth): Result<Unit> {
        return dataSource.saveHistoricalRecord(record)
    }

    override fun observeHealthRecords(): Flow<List<SmartwatchHealth>> {
        return dataSource.observeHealthRecords()
    }

    override suspend fun updatePhotoUri(photoUri: String): Result<Unit> {
        return dataSource.updatePhotoUri(photoUri)
    }
}
