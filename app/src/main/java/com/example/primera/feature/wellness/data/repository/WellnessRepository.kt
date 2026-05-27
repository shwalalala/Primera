package com.example.primera.feature.wellness.data.repository

import com.example.primera.feature.wellness.domain.model.WellnessGoalModel
import kotlinx.coroutines.flow.Flow

interface WellnessRepository {
    fun observeWellnessGoals(): Flow<List<WellnessGoalModel>>
}
