package com.example.primera.feature.goals.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date

class GoalsRepositoryImpl : GoalsRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun observeGoals(): Flow<List<GoalDto>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("goals")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val goals = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        GoalDto(
                            id = doc.id,
                            userId = doc.getString("userId"),
                            title = doc.getString("title"),
                            icon = doc.getString("icon"),
                            currentValue = doc.getDouble("currentValue") ?: 0.0,
                            targetValue = doc.getDouble("targetValue") ?: 0.0,
                            unit = doc.getString("unit"),
                            category = doc.getString("category"),
                            period = doc.getString("period") ?: "Weekly",
                            accentColorHex = doc.getString("accentColorHex"),
                            timestamp = doc.getTimestamp("timestamp")?.toDate()
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(goals)
            }
        awaitClose(registration::remove)
    }

    override suspend fun addGoal(goal: GoalDto): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val goalData = hashMapOf(
                "userId" to userId,
                "title" to goal.title,
                "icon" to goal.icon,
                "currentValue" to goal.currentValue,
                "targetValue" to goal.targetValue,
                "unit" to goal.unit,
                "category" to goal.category,
                "period" to goal.period,
                "accentColorHex" to goal.accentColorHex,
                "timestamp" to (goal.timestamp ?: Date())
            )
            firestore.collection("goals").add(goalData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit> {
        return try {
            firestore.collection("goals").document(goalId)
                .update("currentValue", newValue)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoal(goal: GoalDto): Result<Unit> {
        return try {
            val goalId = goal.id ?: throw Exception("Goal ID is required for update")
            val goalData = hashMapOf(
                "title" to goal.title,
                "icon" to goal.icon,
                "targetValue" to goal.targetValue,
                "currentValue" to goal.currentValue,
                "unit" to goal.unit,
                "accentColorHex" to goal.accentColorHex
            )
            firestore.collection("goals").document(goalId).update(goalData as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            firestore.collection("goals").document(goalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ensureMandatoryGoals(): Result<Unit> {
        return try {
            auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val goals = observeGoals().first()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfToday = calendar.time
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfToday = calendar.time

            val hasHydration = goals.any { 
                it.title == "Hydration" && 
                it.timestamp != null && 
                it.timestamp.after(startOfToday) && 
                it.timestamp.before(endOfToday)
            }
            
            val hasMovement = goals.any { 
                it.title == "Movement" && 
                it.timestamp != null && 
                it.timestamp.after(startOfToday) && 
                it.timestamp.before(endOfToday)
            }

            if (!hasHydration) {
                addGoal(GoalDto(
                    title = "Hydration",
                    icon = "R.drawable.water",
                    currentValue = 0.0,
                    targetValue = 2.5,
                    unit = "L",
                    category = "Wellness",
                    period = "Daily",
                    accentColorHex = "#64B5F6",
                    timestamp = Date()
                ))
            }
            if (!hasMovement) {
                addGoal(GoalDto(
                    title = "Movement",
                    icon = "R.drawable.steps",
                    currentValue = 0.0,
                    targetValue = 30.0,
                    unit = "min",
                    category = "Wellness",
                    period = "Daily",
                    accentColorHex = "#AED581",
                    timestamp = Date()
                ))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
