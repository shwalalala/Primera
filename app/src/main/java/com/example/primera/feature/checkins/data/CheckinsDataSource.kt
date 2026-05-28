package com.example.primera.feature.checkins.data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

        val registration: ListenerRegistration = firestore.collection("checkins")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CheckinLogDto(
                            id = doc.id,
                            type = doc.getString("type") ?: "Check-in",
                            category = doc.getString("category") ?: "Fetal Movement",
                            message = doc.getString("message"),
                            description = doc.getString("description"),
                            timestamp = doc.getTimestamp("timestamp")?.toDate()
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose(registration::remove)
    }

    fun observeUserWeight(): Flow<CheckinUserDto?> = callbackFlow {
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
                if (snapshot != null && snapshot.exists()) {
                    val weightKg = snapshot.getLong("weightKg")?.toInt()
                    val updatedAt = snapshot.getTimestamp("updatedAt")?.toDate()
                    trySend(CheckinUserDto(weightKg, updatedAt))
                } else {
                    trySend(null)
                }
            }
        awaitClose(registration::remove)
    }

    suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val logData = mutableMapOf(
                "userId" to userId,
                "type" to log.type,
                "category" to log.category,
                "message" to log.message,
                "description" to log.description,
                "timestamp" to (log.timestamp ?: Date())
            )
            
            if (log.id != null) {
                firestore.collection("checkins").document(log.id).set(logData).await()
            } else {
                firestore.collection("checkins").add(logData).await()
            }
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
