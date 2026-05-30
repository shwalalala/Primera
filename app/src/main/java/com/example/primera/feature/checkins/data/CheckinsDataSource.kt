package com.example.primera.feature.checkins.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
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
                            timestamp = doc.getTimestamp("timestamp")?.toDate(),
                            hasWarning = doc.getBoolean("hasWarning") ?: false,
                            warningMessage = doc.getString("warningMessage")
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

        val registration: ListenerRegistration = firestore.collection("users")
            .document(userId)
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
            val userId = auth.currentUser?.uid
                ?: throw Exception("User not authenticated")

            val currentDescription = log.description.orEmpty()
            val currentSymptoms = extractSymptomsFromDescription(currentDescription)

            val warningMessage = detectThreeDayCheckinSymptomPattern(
                userId = userId,
                currentSymptoms = currentSymptoms,
                currentEditingId = log.id
            )

            val logData = mutableMapOf<String, Any?>(
                "userId" to userId,
                "type" to log.type,
                "category" to log.category,
                "message" to log.message,
                "description" to log.description,
                "timestamp" to (log.timestamp ?: Date()),
                "hasWarning" to (warningMessage != null),
                "warningMessage" to warningMessage
            )

            if (log.id != null) {
                firestore.collection("checkins")
                    .document(log.id)
                    .set(logData)
                    .await()
            } else {
                firestore.collection("checkins")
                    .add(logData)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserWeight(weightKg: Int): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("User not authenticated")

            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "weightKg" to weightKg,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun detectThreeDayCheckinSymptomPattern(
        userId: String,
        currentSymptoms: List<String>,
        currentEditingId: String?
    ): String? {
        if (currentSymptoms.isEmpty()) return null

        val startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -2)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val recentLogs = firestore.collection("checkins")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .get()
            .await()
            .documents
            .filter { document ->
                currentEditingId == null || document.id != currentEditingId
            }

        val previousSymptoms = recentLogs
            .flatMap { document ->
                val description = document.getString("description").orEmpty()
                extractSymptomsFromDescription(description)
            }
            .map { it.lowercase().trim() }
            .toSet()

        val repeatedSymptom = currentSymptoms.firstOrNull { symptom ->
            previousSymptoms.contains(symptom.lowercase().trim())
        }

        return repeatedSymptom?.let { symptom ->
            "You have logged $symptom repeatedly within the last 3 days. Please monitor your condition and consider consulting a healthcare professional if it continues or worsens."
        }
    }

    private fun extractSymptomsFromDescription(description: String): List<String> {
        val symptomLabel = "Symptoms:"
        val start = description.indexOf(symptomLabel)

        if (start == -1) return emptyList()

        val contentStart = start + symptomLabel.length
        val end = description.indexOf(";", contentStart).let { semicolonIndex ->
            if (semicolonIndex == -1) description.length else semicolonIndex
        }

        return description.substring(contentStart, end)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}