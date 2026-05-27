package com.example.primera.feature.health.data.datasource

import com.example.primera.feature.health.data.dto.HealthLogDto
import com.example.primera.feature.profile.data.dto.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeHealthStats(userId: String): Flow<UserProfileDto?> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val stats = snapshot.toObject(UserProfileDto::class.java)
                    trySend(stats)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun observeRecentLogs(userId: String, limit: Int = 5): Flow<List<HealthLogDto>> = callbackFlow {
        val subscription = firestore.collection("activity_logs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // This is where the index crash happens. Catching it here prevents process death.
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { it.toObject(HealthLogDto::class.java) } ?: emptyList()
                trySend(logs)
            }
        awaitClose { subscription.remove() }
    }
}
