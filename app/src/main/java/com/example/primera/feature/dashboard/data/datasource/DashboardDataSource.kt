package com.example.primera.feature.dashboard.data.datasource

import com.example.primera.feature.dashboard.data.dto.HealthLogDto
import com.example.primera.feature.dashboard.data.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val fullName = snapshot.getString("fullName")
                        val dueDateTimestamp = snapshot.getTimestamp("dueDate")
                        val dueDate = dueDateTimestamp?.toDate()
                        val steps = snapshot.getLong("steps")
                        val stepsGoal = snapshot.getLong("stepsGoal")
                        val heartRateBpm = snapshot.getLong("heartRateBpm")
                        val sleepHours = snapshot.getLong("sleepHours")
                        val sleepMinutes = snapshot.getLong("sleepMinutes")
                        
                        val userDto = UserDto(
                            fullName = fullName,
                            dueDate = dueDate,
                            steps = steps,
                            stepsGoal = stepsGoal,
                            heartRateBpm = heartRateBpm,
                            sleepHours = sleepHours,
                            sleepMinutes = sleepMinutes
                        )
                        trySend(userDto)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeRecentLogs(): Flow<List<HealthLogDto>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("activity_logs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // We don't necessarily want to close the flow on error if it's transient
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val type = doc.getString("type")
                        val category = doc.getString("category")
                        val message = doc.getString("message")
                        val description = doc.getString("description")
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()
                        HealthLogDto(
                            type = type,
                            category = category,
                            message = message,
                            description = description,
                            timestamp = timestamp
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }
}
