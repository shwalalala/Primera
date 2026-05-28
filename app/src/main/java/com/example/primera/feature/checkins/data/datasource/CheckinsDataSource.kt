package com.example.primera.feature.checkins.data.datasource

import com.example.primera.feature.checkins.data.dto.CheckinLogDto
import com.example.primera.feature.checkins.data.dto.CheckinUserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class CheckinsDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun observeLogs(): Flow<List<CheckinLogDto>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("activity_logs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CheckinLogDto(
                            id = doc.id,
                            type = doc.getString("type"),
                            category = doc.getString("category"),
                            message = doc.getString("message"),
                            description = doc.getString("description"),
                            timestamp = doc.getTimestamp("timestamp")?.toDate()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    fun observeUserWeight(): Flow<CheckinUserDto?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val weightKg = snapshot.getLong("weightKg")?.toInt()
                    val updatedAt = snapshot.getTimestamp("updatedAt")?.toDate()
                    trySend(CheckinUserDto(weightKg, updatedAt))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val logData = hashMapOf(
                "userId" to userId,
                "type" to log.type,
                "category" to log.category,
                "message" to log.message,
                "description" to log.description,
                "timestamp" to (log.timestamp ?: Date())
            )
            firestore.collection("activity_logs").add(logData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserWeight(weightKg: Int): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "weightKg" to weightKg,
                        "updatedAt" to Date()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
