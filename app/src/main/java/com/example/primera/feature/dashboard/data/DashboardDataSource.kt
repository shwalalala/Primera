package com.example.primera.feature.dashboard.data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class DashboardDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun observeUserProfile(): Flow<UserDto?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                val userDto = if (snapshot != null && snapshot.exists()) {
                    try {
                        UserDto(
                            fullName = snapshot.getString("fullName"),
                            dueDate = (snapshot.getTimestamp("eddDate") ?: snapshot.getTimestamp("dueDate"))?.toDate(),
                            steps = snapshot.getLong("steps") ?: 0L,
                            stepsGoal = snapshot.getLong("stepsGoal") ?: 8000L,
                            heartRateBpm = snapshot.getLong("heartRateBpm") ?: 0L,
                            sleepHours = snapshot.getLong("sleepHours") ?: 0L,
                            sleepMinutes = snapshot.getLong("sleepMinutes") ?: 0L,
                            spO2 = snapshot.getLong("spO2"),
                            heightCm = snapshot.getLong("heightCm")
                        )
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
                trySend(userDto)
            }
            
        awaitClose(registration::remove)
    }

    fun observeRecentLogs(): Flow<List<HealthLogDto>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("checkins")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        HealthLogDto(
                            id = doc.id,
                            type = doc.getString("type"),
                            category = doc.getString("category"),
                            message = doc.getString("message"),
                            description = doc.getString("description"),
                            timestamp = doc.getTimestamp("timestamp")?.toDate(),
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose(registration::remove)
    }

    suspend fun updateStepsGoal(goal: Long): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            firestore.collection("users").document(userId)
                .update("stepsGoal", goal)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHealthData(
        steps: Long,
        heartRate: Long,
        sleepHours: Long,
        sleepMinutes: Long,
        spO2: Long? = null
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val updates = mutableMapOf<String, Any>(
                "steps" to steps,
                "heartRateBpm" to heartRate,
                "sleepHours" to sleepHours,
                "sleepMinutes" to sleepMinutes,
                "updatedAt" to Date()
            )
            spO2?.let { updates["spO2"] = it }
            
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
