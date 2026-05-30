package com.example.primera.feature.goals.data

import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun observeGoals(): Flow<List<GoalDto>>
    suspend fun addGoal(goal: GoalDto): Result<Unit>
    suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit>
    suspend fun updateGoal(goal: GoalDto): Result<Unit>
    suspend fun deleteGoal(goalId: String): Result<Unit>
    suspend fun ensureMandatoryGoals(): Result<Unit>
}
