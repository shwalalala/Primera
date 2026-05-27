package com.example.primera.feature.wellness.data.repository

import com.example.primera.feature.wellness.domain.model.WellnessGoalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WellnessRepositoryImpl @Inject constructor() : WellnessRepository {

    override fun observeWellnessGoals(): Flow<List<WellnessGoalModel>> {
        return flowOf(
            listOf(
                WellnessGoalModel("Hydration", "💧", 1.8f, 2.5f, "L", "FF7986CB"),
                WellnessGoalModel("Prenatal Yoga", "🧘", 15f, 30f, "min", "FFBA68C8")
            )
        )
    }
}
