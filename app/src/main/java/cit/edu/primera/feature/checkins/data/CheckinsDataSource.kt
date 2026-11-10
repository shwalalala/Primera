package cit.edu.primera.feature.checkins.data

import cit.edu.primera.core.util.CryptoManager
import cit.edu.primera.core.util.currentUserIdFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class CheckinsDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLogs(): Flow<List<CheckinLogDto>> =
        auth.currentUserIdFlow().flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration: ListenerRegistration = firestore.collection("checkins")
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            val logs = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    val rawType = doc.getString("type")
                                    val rawCategory = doc.getString("category")
                                    val rawMessage = doc.getString("message")
                                    val rawDescription = doc.getString("description")
                                    val rawWarningMessage = doc.getString("warningMessage")

                                    CheckinLogDto(
                                        id = doc.id,
                                        type = CryptoManager.decrypt(rawType) ?: "Check-in",
                                        category = CryptoManager.decrypt(rawCategory)
                                            ?: "Fetal Movement",
                                        message = CryptoManager.decrypt(rawMessage),
                                        description = CryptoManager.decrypt(rawDescription),
                                        timestamp = doc.getTimestamp("timestamp")?.toDate(),
                                        hasWarning = doc.getBoolean("hasWarning") ?: false,
                                        warningMessage = CryptoManager.decrypt(rawWarningMessage)
                                    )
                                } catch (_: Exception) {
                                    null
                                }
                            }?.sortedByDescending { it.timestamp ?: Date(0) } ?: emptyList()

                            trySend(logs)
                        }

                    awaitClose(registration::remove)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUserWeight(): Flow<CheckinUserDto?> =
        auth.currentUserIdFlow().flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(null)
            } else {
                callbackFlow {
                    val registration: ListenerRegistration = firestore.collection("users")
                        .document(userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(null)
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                val weightRaw = snapshot.get("weightKg")
                                val weightKg = CryptoManager.decryptInt(weightRaw)
                                val updatedAt = snapshot.getTimestamp("updatedAt")?.toDate()

                                trySend(CheckinUserDto(weightKg, updatedAt))
                            } else {
                                trySend(null)
                            }
                        }

                    awaitClose(registration::remove)
                }
            }
    }

    suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: "anonymous"

            val currentDescription = log.description.orEmpty()
            val currentSymptoms = extractSymptomsFromDescription(currentDescription)

            val warningMessage = detectThreeDayCheckinSymptomPattern(
                userId = userId,
                currentSymptoms = currentSymptoms,
                currentEditingId = log.id
            )

            val logData = mutableMapOf<String, Any?>(
                "userId" to userId,
                "type" to CryptoManager.encrypt(log.type),
                "category" to CryptoManager.encrypt(log.category),
                "message" to CryptoManager.encrypt(log.message),
                "description" to CryptoManager.encrypt(log.description),
                "timestamp" to (log.timestamp ?: Date()),
                "hasWarning" to (warningMessage != null),
                "warningMessage" to CryptoManager.encrypt(warningMessage)
            )

            if (!log.id.isNullOrBlank()) {
                firestore.collection("checkins")
                    .document(log.id)
                    .set(logData, SetOptions.merge())
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
            val userId = auth.currentUser?.uid ?: "anonymous"

            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "weightKg" to CryptoManager.encryptInt(weightKg),
                        "updatedAt" to Date()
                    ),
                    SetOptions.merge()
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

        return try {
            val startDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -2)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val recentLogs = firestore.collection("checkins")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .filter { document ->
                    currentEditingId == null || document.id != currentEditingId
                }
                .filter { document ->
                    val ts = document.getTimestamp("timestamp")?.toDate()
                    ts != null && !ts.before(startDate)
                }

            val previousSymptoms = recentLogs
                .flatMap { document ->
                    val rawDescription = document.getString("description").orEmpty()
                    val description = CryptoManager.decrypt(rawDescription).orEmpty()
                    extractSymptomsFromDescription(description)
                }
                .map { it.lowercase().trim() }
                .toSet()

            val repeatedSymptom = currentSymptoms.firstOrNull { symptom ->
                previousSymptoms.contains(symptom.lowercase().trim())
            }

            repeatedSymptom?.let { symptom ->
                "You have logged $symptom repeatedly within the last 3 days. Please monitor your condition and consider consulting a healthcare professional if it continues or worsens."
            }
        } catch (_: Exception) {
            null
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
